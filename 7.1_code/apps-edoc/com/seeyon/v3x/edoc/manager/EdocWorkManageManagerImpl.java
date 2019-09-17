package com.seeyon.v3x.edoc.manager;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.ctp.common.affair.util.AffairUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.StatUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.dao.EdocSummaryDao;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.exception.EdocException;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

/**
 * 绩效管理接口实现
 * 
 * @author 杨帆
 * 
 */
public class EdocWorkManageManagerImpl implements EdocWorkManageManager {

	private static final Log LOGGER = LogFactory.getLog(EdocWorkManageManagerImpl.class);
	
	private EdocSummaryDao edocSummaryDao;
	private OrgManager orgManager;

	public void setEdocSummaryDao(EdocSummaryDao edocSummaryDao) {
		this.edocSummaryDao = edocSummaryDao;
	}

	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}

	@Override
	public List<EdocSummaryModel> queryEdocList(Long memberId, int type,
			Date beginDate, Date endDate,int coverTime, String processType, boolean isPage) throws EdocException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();

		User user = CurrentUser.get();
		long user_id = user.getId();
		// 获取代理相关信息
		List<AgentModel> _agentModelList = MemberAgentBean.getInstance()
				.getAgentModelList(user_id);
		List<AgentModel> _agentModelToList = MemberAgentBean.getInstance()
				.getAgentModelToList(user_id);
		List<AgentModel> agentModelList = null;
		boolean agentToFlag = false;
		boolean agentFlag = false;
		if (_agentModelList != null && !_agentModelList.isEmpty()) {
			agentModelList = _agentModelList;
			agentFlag = true;
		} else if (_agentModelToList != null && !_agentModelToList.isEmpty()) {
			agentModelList = _agentModelToList;
			agentToFlag = true;
		}
		Map<Integer, AgentModel> agentModelMap = new HashMap<Integer, AgentModel>();
		AgentModel edocAgent = null;
		if (agentModelList != null && !agentModelList.isEmpty()) {
			for (AgentModel agentModel : agentModelList) {
    			if(agentModel.isHasEdoc()){
					edocAgent = agentModel;
					agentModelMap.put(ApplicationCategoryEnum.edoc.key(),
							agentModel);
    			}
		    	
			}
		}

		StringBuffer hql = new StringBuffer();
		hql.append("select ").append(edocSelectAffair);
		parameterMap.put("memberId", memberId);
		//查询已归档数据单独拼hql
		if (type==10) {
			hql.append(" from DocResourcePO doc,CtpAffair affair,EdocSummary summary");
			hql.append(" where doc.mimeTypeId=2");
			switch (coverTime) {
			  case 0: // 未超期
				  hql.append(" and summary.coverTime=false ");
				  break;
			  case 1: // 已超期,已归档使用流程超期
				  hql.append(" and summary.coverTime=true "); 
				  break;
			}
			hql.append(" and (");
			hql.append(" (summary.id=affair.objectId and affair.objectId=doc.sourceId and affair.state=2)");
			hql.append(" or (summary.id=affair.objectId and affair.id=doc.sourceId and affair.state in (2,3,4) and affair.memberId = :memberId)");
			hql.append(" )");
			//hql.append(" and ((affair.state=:state1 or affair.state=:state2) and affair.activityId is not null");
			//hql.append(" or affair.state=:state2 and summary.archiveId is not null");
			hql.append(" and doc.createUserId =:memberId");
			if (beginDate != null) {
				hql.append(" and doc.createTime > :timestamp1");
			}
			if (endDate != null) {
				hql.append(" and doc.createTime <= :timestamp2");
			}
			hql.append(" order by doc.createTime desc");
//			parameterMap.put("state1", StateEnum.col_done.key());
//			parameterMap.put("state2", StateEnum.col_sent.key());
			setBeginAndEndTime(beginDate, endDate,parameterMap);
		} else {
			hql.append(" from CtpAffair as affair,EdocSummary as summary, OrgMember as mem");
			//.append(V3xOrgMember.class.getName()).append(" as mem");
			hql.append(" where affair.objectId=summary.id and affair.senderId=mem.id and ");
			hql.append(" affair.memberId=:memberId");
			hql.append(" and affair.app in(:appList)  ");
			//除了已发和已归档使用流程超期（summary.coverTime），其他都用节点超期（待办、暂存：当前-收到；已办：完成-收到）(affair.coverTime)
			switch (coverTime) {
			  case 0: // 未超期
				  hql.append(" and affair.coverTime=false ");
				  break;
			  case 1: // 已超期
				  if(type==3||type==5||type==7||type==9){//除了已发使用流程超期（summary.coverTime）
					  hql.append(" and summary.coverTime=true "); 
				  }else{//其他都用节点超期（affair.coverTime）
					  hql.append(" and affair.coverTime=true "); 
				  }
				  break;
			}
			
			// 已办的列表中只查询正常处理流程的，不包括已签收，已发送，已登记之类
			//if (type == 2 || type == 4 || type == 6 || type == 8) {
				//parameterMap.put("appList",
				//		this.getAppNormalList(ApplicationCategoryEnum.edoc.key()));
			//} else {
			parameterMap.put("appList",
					this.getAppList(ApplicationCategoryEnum.edoc.key()));
			//}
			
			//type == 22 查询超期统计（已办+当前待办 其中待办不根据时间查询,所以时间查询需要在后面单独处理）
			if ((type == 2 || type == 4 || type == 6 || type == 8) && (beginDate != null || endDate != null) && (type != 22)) { //已办 只查处理时间范围内
				hql.append(" and (affair.completeTime > :timestamp1 and affair.completeTime < :timestamp2) ");
			}else if((type == 3 ||type == 5 ||type == 7) && (type != 22)){ //已发
				hql.append(" and ((affair.createDate > :timestamp1 and affair.createDate < :timestamp2)");
				hql.append(" or (affair.receiveTime > :timestamp1 and affair.receiveTime < :timestamp2)");
				hql.append(" or (affair.completeTime > :timestamp1 and affair.completeTime < :timestamp2))");
			}else if ( type == 9 && (beginDate != null || endDate != null) && (type != 22)) {
				ifTheDateIsNull(beginDate, endDate, hql, "timestamp1", "timestamp2");
			}else if((type == 0 || type == 1) && (beginDate != null || endDate != null) && (type != 22)){
				hql.append(" and (affair.receiveTime > :timestamp1 and affair.receiveTime < :timestamp2) ");
			}
			Map map = StatUtil.getStatDate();
			switch (type) {
			case 0: // 指定期限待办
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_pending.key());
				setBeginAndEndTime(beginDate, endDate,parameterMap);
				break;
			case 1: // 指定期限暂存待办
				hql.append(" and affair.state=:state and affair.subState= :subState");
				parameterMap.put("state", StateEnum.col_pending.key());
				parameterMap.put("subState", SubStateEnum.col_pending_ZCDB.key());
				setBeginAndEndTime(beginDate, endDate,parameterMap);
				break;
			case 2: // 本日已办
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_done.key());
				parameterMap.put("timestamp1", map.get("BeginOfDay"));
				parameterMap.put("timestamp2", map.get("EndOfDay"));
				break;
			case 3: // 本日已发
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_sent.key());
				parameterMap.put("timestamp1", map.get("BeginOfDay"));
				parameterMap.put("timestamp2", map.get("EndOfDay"));
				break;
			case 4: // 本周已办
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_done.key());
				parameterMap.put("timestamp1", map.get("BeginOfWeek"));
				parameterMap.put("timestamp2", map.get("EndOfWeek"));
				break;
			case 5: // 本周已发
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_sent.key());
				parameterMap.put("timestamp1", map.get("BeginOfWeek"));
				parameterMap.put("timestamp2", map.get("EndOfWeek"));
				break;
			case 6: // 本月已办
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_done.key());
				parameterMap.put("timestamp1", map.get("BeginOfMonth"));
				parameterMap.put("timestamp2", map.get("EndOfMonth"));
				break;
			case 7: // 本月已发
				hql.append(" and affair.state=:state ");
				parameterMap.put("state", StateEnum.col_sent.key());
				parameterMap.put("timestamp1", map.get("BeginOfMonth"));
				parameterMap.put("timestamp2", map.get("EndOfMonth"));
				break;
			case 8: // 指定期限的已办
				hql.append(" and affair.state=:state ");
			    if(Strings.isNotBlank(processType) && "freeCol".equals(processType)){
			        hql.append(" and summary.templeteId is null ");
			    }else if(Strings.isNotBlank(processType) && "templateCol".equals(processType)){
			        hql.append(" and summary.templeteId is not null ");
			    }
				parameterMap.put("state", StateEnum.col_done.key());
				setBeginAndEndTime(beginDate, endDate,parameterMap);
				break;
			case 9: // 指定期限的已发
				hql.append(" and affair.state=:state ");
				if(Strings.isNotBlank(processType) && "freeCol".equals(processType)){
                    hql.append("and summary.templeteId is null ");
                }else if(Strings.isNotBlank(processType) && "templateCol".equals(processType)){
                    hql.append("and summary.templeteId is not null ");
                }
				parameterMap.put("state", StateEnum.col_sent.key());
				setBeginAndEndTime(beginDate, endDate,parameterMap);
				break;
			case 22://已办+当前待办，待办不根据时间来查
                hql.append(" and ((affair.state=:pendingState) or");
                hql.append(" (affair.state=:doneState and affair.completeTime > :timestamp1 and affair.completeTime < :timestamp2))");
                parameterMap.put("doneState", StateEnum.col_done.key());
                parameterMap.put("pendingState", StateEnum.col_pending.key());
                setBeginAndEndTime(beginDate, endDate,parameterMap);
                break;
			case 23://流程已发已办统计 统计维度:已发+已办
			    hql.append(" and ((affair.state=:sentState and affair.createDate > :timestamp1 and affair.createDate < :timestamp2) or");
                hql.append(" (affair.state=:doneState and affair.completeTime > :timestamp1 and affair.completeTime < :timestamp2)) ");
                if(Strings.isNotBlank(processType) && "freeCol".equals(processType)){
                    hql.append("and summary.templeteId is null ");
                }else if(Strings.isNotBlank(processType) && "templateCol".equals(processType)){
                    hql.append("and summary.templeteId is not null ");
                }
                parameterMap.put("doneState", StateEnum.col_done.key());
                parameterMap.put("sentState", StateEnum.col_sent.key());
                setBeginAndEndTime(beginDate, endDate,parameterMap);
                break;
			}
			if (type == 0 || type == 1) {
				hql.append(" order by affair.receiveTime desc");
			} else if (type == 2 || type == 4 || type == 6 || type == 8) {
				hql.append(" order by affair.completeTime desc");
			} else {
				hql.append(" order by affair.createDate desc");
			}			
		}
		
		


		List result = null;
		if(isPage){
		    result = edocSummaryDao.find(hql.toString(), parameterMap);
		}else{
		    result = edocSummaryDao.find(hql.toString(), -1, -1, parameterMap);
		}

		List<EdocSummaryModel> models = new ArrayList<EdocSummaryModel>();
		for (int i = 0; i < result.size(); i++) {
			Object[] object = (Object[]) result.get(i);
			CtpAffair affair = new CtpAffair();
			EdocSummary summary = new EdocSummary();
			makeEdoc(object, summary, affair);

			try {
				V3xOrgMember member = orgManager.getEntityById(
						V3xOrgMember.class, summary.getStartUserId());
				summary.setStartMember(member);
			} catch (BusinessException e) {
				LOGGER.error("", e);
			}

			// 开始组装最后返回的结果
			EdocSummaryModel model = new EdocSummaryModel();
			int affairState = affair.getState();
			if (affairState == StateEnum.col_waitSend.key()) {
				model.setWorkitemId(null);
				model.setCaseId(null);
				model.setStartDate(new java.sql.Date(summary.getCreateTime()
						.getTime()));
				model.setSummary(summary);
				model.setAffairId(affair.getId());

			} else if (affairState == StateEnum.col_sent.key()) {
				model.setWorkitemId(null);
				model.setCaseId(summary.getCaseId() + "");
				model.setStartDate(new java.sql.Date(summary.getCreateTime()
						.getTime()));
				model.setSummary(summary);
				model.setAffairId(affair.getId());
				// 设置流程是否超期标志
				java.sql.Timestamp startDate = summary.getCreateTime();
				java.sql.Timestamp finishDate = summary.getCompleteTime();
				Date now = new Date(System.currentTimeMillis());
				if (summary.getDeadline() != null && summary.getDeadline() != 0) {
					Long deadline = summary.getDeadline() * 60000;
					if (finishDate == null) {
						if ((now.getTime() - startDate.getTime()) > deadline) {
							summary.setWorklfowTimeout(true);
						}
					} else {
						Long expendTime = summary.getCompleteTime().getTime()
								- summary.getCreateTime().getTime();
						if ((deadline - expendTime) < 0) {
							summary.setWorklfowTimeout(true);
						}
					}
				}
			}else {
				model.setWorkitemId(affair.getObjectId() + "");
				model.setCaseId(summary.getCaseId() + "");
				model.setSummary(summary);
				model.setAffairId(affair.getId());
			}

			if (affairState == StateEnum.col_waitSend.key()) {
				model.setEdocType(EdocSummaryModel.EDOCTYPE.WaitSend.name());
			} else if (affairState == StateEnum.col_sent.key()) {
				model.setEdocType(EdocSummaryModel.EDOCTYPE.Sent.name());
			} else if (affairState == StateEnum.col_done.key()) {
				model.setEdocType(EdocSummaryModel.EDOCTYPE.Done.name());
			} else if (affairState == StateEnum.col_pending.key()) {
				model.setEdocType(EdocSummaryModel.EDOCTYPE.Pending.name());
			}

			model.setFinshed(summary.getCompleteTime() != null);

			model.setAffair(affair);
			model.setBodyType(affair.getBodyType());

			// 公文状态
			Integer sub_state = affair.getSubState();
			if (sub_state != null) {
				model.setState(sub_state.intValue());
			}

			// 是否跟踪
			Integer isTrack = affair.getTrack();
			if (isTrack != null) {
				model.setTrack(isTrack);
			}

			// 催办次数
			Integer hastenTimes = affair.getHastenTimes();
			if (hastenTimes != null) {
				model.setHastenTimes(hastenTimes);
			}

			// 检查是否有附件
			model.setHasAttachments(AffairUtil.isHasAttachments(affair));

			// 是否超期
			Boolean overtopTime = affair.isCoverTime();
			if (overtopTime != null) {
				model.setOvertopTime(overtopTime.booleanValue());
			}

			// 提前提醒
			Long advanceRemind = affair.getRemindDate();
			if (advanceRemind == null) {
				advanceRemind = 0L;
			}
			model.setAdvanceRemindTime(advanceRemind);

			// 协同处理期限
			Long deadLine = affair.getDeadlineDate();
			if (deadLine == null) {
				deadLine = 0L;
			}
			model.setDeadLine(deadLine);
			model.setDeadlineDisplay(EdocHelper.getDeadLineName(affair.getExpectedProcessTime()));
			// 是否代理
			if (affairState == StateEnum.col_done.key()) {
				if (affair.getTransactorId() != null) {
					try {
						V3xOrgMember member = orgManager.getMemberById(affair
								.getTransactorId());
						model.setProxyName(member.getName());
						model.setProxy(true);
					} catch (BusinessException e) {
						LOGGER.error("", e);
					}
				}
			}

			if (affairState == StateEnum.col_pending.key() && agentFlag
					&& affair.getMemberId().longValue() != user.getId() && edocAgent!=null) {
				Long proxyMemberId = edocAgent.getAgentToId();
				V3xOrgMember member = null;
				try {
					member = orgManager.getMemberById(proxyMemberId);
				} catch (BusinessException e) {
					LOGGER.error("", e);
				}
				if(member != null ){
					model.setProxyName(member.getName());	
				}
				model.setProxy(true);
			}

			if (affair.getCompleteTime() != null) {
				model.setDealTime(new java.sql.Date(affair.getCompleteTime()
						.getTime()));
			}
			models.add(model);
		}
		return models;
	}

	private static final String selectSummary = "summary.id,summary.startUserId,summary.caseId,summary.completeTime"
			+ ",summary.subject,summary.secretLevel,summary.identifier,summary.docMark,summary.serialNo,summary.createTime"
			+ ",summary.sendTo,summary.issuer,summary.signingDate,summary.deadline,summary.startTime,summary.copies,summary.createPerson,summary.sendUnit,summary.hasArchive";

	private static final String edocSelectAffair = selectSummary
			+ ",affair.id,"
			+ "affair.state,"
			+ "affair.subState,"
			+ "affair.track,"
			+ "affair.hastenTimes,"
			+ "affair.coverTime,"
			+ "affair.remindDate,"
			+ "affair.deadlineDate,"
			+ "affair.receiveTime,"
			+ "affair.completeTime,"
			+ "affair.createDate,"
			+ "affair.memberId," 
			+"affair.bodyType," 
			+"affair.transactorId," 
			+"affair.app," 
			+"affair.objectId," 
			+"affair.subObjectId," 
			+"affair.importantLevel, "
			+"affair.expectedProcessTime ";

	private static void makeEdoc(Object[] object, EdocSummary summary,
			CtpAffair affair) {
		int n = 0;
		summary.setId((Long) object[n++]);
		summary.setStartUserId((Long) object[n++]);
		summary.setCaseId((Long) object[n++]);
		summary.setCompleteTime((Timestamp) object[n++]);
		summary.setSubject((String) object[n++]);
		summary.setSecretLevel((String) object[n++]);
		summary.setIdentifier((String) object[n++]);
		summary.setDocMark((String) object[n++]);
		summary.setSerialNo((String) object[n++]);
		summary.setCreateTime((Timestamp) object[n++]);
		summary.setSendTo((String) object[n++]);
		summary.setIssuer((String) object[n++]);
		summary.setSigningDate((java.sql.Date) object[n++]);
		summary.setDeadline((Long) object[n++]);
		summary.setStartTime((Timestamp) object[n++]);
		summary.setCopies((Integer) object[n++]);
		summary.setCreatePerson((String) object[n++]);
		summary.setSendUnit((String) object[n++]);
		summary.setHasArchive((Boolean) object[n++]);

		affair.setId((Long) object[n++]);
		affair.setState((Integer) object[n++]);
		affair.setSubState((Integer) object[n++]);
		affair.setTrack((Integer) object[n++]);
		affair.setHastenTimes((Integer) object[n++]);
		affair.setCoverTime((Boolean) object[n++]);
		affair.setRemindDate((Long) object[n++]);
		affair.setDeadlineDate((Long) object[n++]);
		affair.setReceiveTime((Timestamp) object[n++]);
		affair.setCompleteTime((Timestamp) object[n++]);
		affair.setCreateDate((Timestamp) object[n++]);
		affair.setMemberId((Long) object[n++]);
		affair.setBodyType((String) object[n++]);
		affair.setTransactorId((Long) object[n++]);
		affair.setApp((Integer) object[n++]);
		affair.setObjectId((Long) object[n++]);
		affair.setSubObjectId((Long) object[n++]);
		affair.setImportantLevel((Integer) object[n++]);
		affair.setExpectedProcessTime((Date)object[n++]);
		summary.setImportantLevel(affair.getImportantLevel());
	}

    /**
     * 封装应用ID，如app=4为公文，则需要查询其他公文类别ID
     * @param app
     * @return
     */
    private static List<Integer> getAppNormalList(int app){
        List<Integer> appList = new ArrayList<Integer>();
        if(app == ApplicationCategoryEnum.edoc.key()){
            appList.add(ApplicationCategoryEnum.edoc.key());
            appList.add(ApplicationCategoryEnum.edocRec.key());
            appList.add(ApplicationCategoryEnum.edocSend.key());
            appList.add(ApplicationCategoryEnum.edocSign.key());
        }
        else{
            appList.add(app);
        }
        return appList;
    }
    
    /**
     * 封装应用ID，如app=4为公文，则需要查询其他公文类别ID
     * @param app
     * @return
     */
    private static List<Integer> getAppList(int app){
        List<Integer> appList = new ArrayList<Integer>();
        if(app == ApplicationCategoryEnum.edoc.key()){
        	  appList.add(ApplicationCategoryEnum.edoc.key());
        	  appList.add(ApplicationCategoryEnum.edocRec.key());
        	  appList.add(ApplicationCategoryEnum.edocSend.key());
        	  appList.add(ApplicationCategoryEnum.edocSign.key());
//            appList.add(ApplicationCategoryEnum.edoc.key());
//            appList.add(ApplicationCategoryEnum.edocRegister.key());
//            appList.add(ApplicationCategoryEnum.exchange.key());
//            appList.add(ApplicationCategoryEnum.exSend.key());
//            appList.add(ApplicationCategoryEnum.exSign.key());
//            appList.add(ApplicationCategoryEnum.edocRecDistribute.key());
        }
        else{
            appList.add(app);
        }
        return appList;
    }
    
	//如果时间为空 组成的sql 语句
	private void ifTheDateIsNull(Date beginDate,Date endDate,StringBuffer hql,String begin,String end){
		if(beginDate == null && endDate == null) return ;
		hql.append(" and (("+(beginDate !=null?(endDate !=null?" affair.createDate > :"+begin+" and affair.createDate < :"+end+" ":" affair.createDate > :"+begin+" "):(endDate != null?" affair.createDate < :"+end+" ":""))+")");
        hql.append(" or ("+(beginDate != null? (endDate != null?" affair.receiveTime > :"+begin+" and affair.receiveTime < :"+end+" ":" affair.receiveTime > :"+begin+" "):(endDate != null?" affair.receiveTime < :"+end+" ":""))+")");
        hql.append(" or ("+(beginDate !=null ? (endDate != null?" affair.completeTime > :"+begin+" and affair.completeTime < :"+end+" ":" affair.completeTime > :"+begin+" "):(endDate !=null?" affair.completeTime < :"+end+" ":""))+"))");
	}
	
	
	private void setBeginAndEndTime(Date beginDate, Date endDate, Map<String, Object> parameterMap){
		if (beginDate != null) {
			Timestamp beginOfDateTS = new Timestamp(beginDate.getTime());
			parameterMap.put("timestamp1", beginOfDateTS);
		}
		if (endDate != null) {
			Timestamp endOfDateTS = new Timestamp(endDate.getTime());

			parameterMap.put("timestamp2", endOfDateTS);
		}
	}
}
