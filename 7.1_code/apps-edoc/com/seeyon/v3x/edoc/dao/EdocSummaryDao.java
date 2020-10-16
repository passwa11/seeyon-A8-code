package com.seeyon.v3x.edoc.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.seeyon.apps.edoc.bo.SimpleEdocSummary;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.po.affair.CtpAffair;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.taglibs.functions.Functions;
import com.seeyon.v3x.edoc.domain.EdocRegister;
import com.seeyon.v3x.edoc.domain.EdocRegisterCondition;
import com.seeyon.v3x.edoc.domain.EdocSubjectWrapRecord;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.listener.EdocWorkflowManageHandler;
import com.seeyon.v3x.edoc.manager.EdocHelper;
import com.seeyon.v3x.edoc.webmodel.EdocSummaryModel;

public class EdocSummaryDao extends BaseHibernateDao<EdocSummary> {
	
	private static final Log log = LogFactory.getLog(EdocSummaryDao.class);

	public void updateEdocSummaryState(Long edocId, int state) {
		String hsql = "update EdocSummary set state=:state where id=:edocId";
		java.util.Map<String, Object> params = new HashMap<String, Object>();
		params.put("state", state);
		params.put("edocId", edocId);
		super.bulkUpdate(hsql, params);
	}
	
	 @SuppressWarnings("unchecked")
		public List<Object[]> getInfoField2RecVo(long summaryId) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("summaryId", summaryId);
			return (List<Object[]>)super.find("select id, docMark,subject,issuer,sendUnit,sendDepartment,startTime from EdocSummary where id =:summaryId", parameterMap);		
		}
		
		@SuppressWarnings("unchecked")
		public List<Object[]> getGovDocTypeAndEdocTypeBySummaryId(long summaryId) {
			Map<String, Object> parameterMap = new HashMap<String, Object>();
			parameterMap.put("summaryId", summaryId);
			return (List<Object[]>)super.find("select govdocType, edocType  from EdocSummary where id =:summaryId", parameterMap);		
		}
		
	 @SuppressWarnings("unchecked")
		public List<Object[]> getLargeFieldSummaryList(List<Long> summaryIdList) {
			if(Strings.isNotEmpty(summaryIdList)) {
				Map<String, Object> parameterMap = new HashMap<String, Object>();
				parameterMap.put("summaryIdList", summaryIdList);
				String hql = "select id, sendDepartment, sendUnit, currentNodesInfo from EdocSummary where id in (:summaryIdList)";
				return DBAgent.find(hql,parameterMap);
			} 
			return null;
		}
	
	/**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param summaryId  公文ID
	 * @param serialNo   内部文号
	 * @param loginAccount  登录单位
	 * @return (1：存在  0：不存在)
	 */
	public int checkSerialNoExsit(String summaryId,String serialNo,Long orgAccountId){
		if(serialNo==null||"".equals(serialNo))return 0;//为空，或者表单中没有这个字段。
		
		StringBuilder sb=new StringBuilder();
		sb.append("from EdocSummary as summary where ");
		if(summaryId!=null && !"".equals(summaryId)){
			sb.append(" summary.id!=? and ");
		}
		sb.append(" summary.serialNo = ?  and summary.orgAccountId=? ");
		
		List<EdocSummary> summarys = null;
		if(summaryId!=null && !"".equals(summaryId)){
		    Object[] values3= {Long.parseLong(summaryId),SQLWildcardUtil.escape(Strings.nobreakSpaceToSpace(serialNo)),orgAccountId};
		    summarys = super.findVarargs(sb.toString(), values3);
		}else { 
		    Object[] values2= {SQLWildcardUtil.escape(Strings.nobreakSpaceToSpace(serialNo)),orgAccountId};
		    summarys = super.findVarargs(sb.toString(), values2);
		}
		if(summarys.size()>0) {
		    /*当文号使用过时，还需要判断一种情况，公文撤销后在待发中删除的这种情况，如果这样，该文号还是能使用*/
		    
		    List<Long> idList = new ArrayList<Long>();
		    for(EdocSummary summary : summarys){
		        idList.add(summary.getId());
		    }
		    String hql = "select s from EdocSummary s,CtpAffair a where s.id = a.objectId " +
		    		" and s.serialNo = :serialNo  and s.orgAccountId=:orgAccountId  and s.id in(:id) " +
		    		" and a.state in (:state) and a.delete=:delete";
		    Map map = new HashMap();
		    map.put("serialNo", SQLWildcardUtil.escape(Strings.nobreakSpaceToSpace(serialNo)));
		    map.put("orgAccountId", orgAccountId);
		    map.put("id", idList);
		    
		    List<Integer> stateList = new ArrayList<Integer>();
		    stateList.add(StateEnum.col_waitSend.key());
		    stateList.add(StateEnum.col_sent.key());
		    map.put("state", stateList);
		    map.put("delete", false);
		    List list = super.find(hql,-1,-1, map);
		    if(list.size()>0){
		        return 1;//找到
		    }else{
		        return 0;    
		    }
		}else{			 //没找到
			return 0;	
		}
	}
	
	//OA-19133  验证客户bug：发送两个公文使用的是相同的公文问号，有一个流程封发了，另一个流程封发的时候不修改公文问号也能提交  
	public int checkDocMarkExsit(String summaryId,String docMark,Long orgAccountId){
        if(docMark==null||"".equals(docMark))return 0;//为空，或者表单中没有这个字段。
        
        StringBuilder sb=new StringBuilder();
        sb.append("from EdocSummary as summary where ");
        if(summaryId!=null && !"".equals(summaryId)){
            sb.append(" summary.id!=? and ");
        }
        sb.append(" summary.docMark = ?  and summary.orgAccountId=? ");
        int count =0;
        
        if(summaryId!=null && !"".equals(summaryId)){
            
            Object[] values3 = {Long.parseLong(summaryId),SQLWildcardUtil.escape(docMark),orgAccountId};
            count = super.getQueryCount(sb.toString(),values3, new Type[]{Hibernate.LONG,Hibernate.STRING,Hibernate.LONG});
        }else {
            Object[] values2 = {SQLWildcardUtil.escape(docMark),orgAccountId};
            count = super.getQueryCount(sb.toString(),values2, new Type[]{Hibernate.STRING,Hibernate.LONG});
        }
        if(count>0) {
            return 1;    //找到
        }else{           //没找到
            return 0;   
        }
    }
	
	
	public void saveOrUpdate(EdocSummary summary)
	{
		if(get(summary.getId()) != null){
			super.getHibernateTemplate().merge(summary);
		}else{
			super.getHibernateTemplate().saveOrUpdate(summary);
		}
	}
	public EdocSummary getSummaryByCaseId(long caseId)
	{
		EdocSummary summary=null;
		String hql="from " + EdocSummary.class.getName() + " as summary where summary.caseId = ? ";
		Object[] values = {caseId};
		List<EdocSummary> list = super.findVarargs(hql, values);
		if (list.size() == 0) {return null;}
		summary = (EdocSummary) list.get(0);		
		return summary;
	}
	public EdocSummary getSummaryByFormId(int formId)
	{
		EdocSummary summary=null;
		String hql="from EdocSummary as summary where summary.formId = ? ";
		Object[] values = {formId};
		List<EdocSummary> list = super.findVarargs(hql, values);
		if (list.size() == 0) {return null;}
		summary = (EdocSummary) list.get(0);		
		return summary;
	}
	
	public EdocSummary getSummaryByProcessId(String processId){
		EdocSummary summary=null;
		String hql="from EdocSummary as summary where summary.processId = ? ";
		Object[] values = {processId};
		List<EdocSummary> list = super.findVarargs(hql, values);
		if (list.size() == 0) {return null;}
		summary = (EdocSummary) list.get(0);		
		return summary;
	}
	
	public boolean isUseMetadataValue(String fieldNames,String value)
	{
		boolean ret=true;
		StringBuilder hql=new StringBuilder("from EdocSummary as summary where");
		String [] fdn=fieldNames.split(",");
		boolean isFirst=true;
		
		Object [] values=new Object[fdn.length];
		Type [] types=new Type[fdn.length];
		int i=0;
		
		for(String fn:fdn)
		{
			if("doc_type".equals(fn)){fn="docType";}
			else if("send_type".equals(fn)){fn="sendType";}
			else if("secret_level".equals(fn)){fn="secretLevel";}
			else if("urgent_level".equals(fn)){fn="urgentLevel";}
			else if("keep_period".equals(fn)){fn="keepPeriod";}
			
			if(isFirst==false){
			    hql.append(" or ");
		    }
			hql.append(" summary.")
			   .append(fn)
			   .append("=?");
			isFirst=false;
			
			if("keepPeriod".equals(fn))
			{
				values[i]=value;
				types[i]=Hibernate.STRING;				
			}
			else
			{
				values[i]=Integer.parseInt(value);
				types[i]=Hibernate.INTEGER;
			}
			i++;
		}
		int iCount=super.getQueryCount(hql.toString(), values, types);
		if(iCount<=0){ret=false;}
		return ret;
	}
	
	public void forceCommit(){
    	super.getSession().flush();
    	super.getSession().clear();
	}
	
	/**       流程效率分析                */
	/*public List<EdocSummary> getEdocSummaryList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select summary " );
		getQueryHql(sb);
		sb.append(" order by summary.runWorkTime,summary.id  ");
		Map<String, Object> parameter = setParameter2Map(accountId,templeteId, workFlowState,
				startDate, endDate);
		return super.find(sb.toString(),-1,-1, parameter);
	}*/
	public List<EdocSummary> getEdocSummaryList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select summary.id,summary.subject,summary.edocType, summary.state,summary.runWorkTime,summary.overWorkTime,summary.createTime,summary.orgAccountId,summary.deadline" );
		getQueryHql(sb);
		sb.append(" order by summary.runWorkTime,summary.id  ");
		Map<String, Object> parameter = setParameter2Map(accountId,templeteId, workFlowState,
				startDate, endDate);
		List<Object[]> objects = super.find(sb.toString(),-1,-1, parameter);
		List<EdocSummary> list = new ArrayList<EdocSummary>();
		EdocSummary summary = null;
		for(Object[] object : objects) {
			int i = 0;
			summary = new EdocSummary();
			summary.setId((Long)object[i++]);
			summary.setSubject((String)object[i++]);
			summary.setEdocType((Integer)object[i++]);
			summary.setState((Integer)object[i++]);
			Object runWorkTime = object[i++];
			summary.setRunWorkTime(((Long)runWorkTime) == null ? 0L : ((Long)runWorkTime));
			Object overWorkTime = object[i++];
			summary.setOverWorkTime(((Long)overWorkTime) == null ? 0L : ((Long)overWorkTime));
			summary.setCreateTime((Timestamp)object[i++]);
			summary.setOrgAccountId((Long)object[i++]);
			Object deadline = object[i++];
			summary.setDeadline(((Long)deadline) == null ? 0L : ((Long)deadline));
			logger.info("setDeadline ...." + summary.getDeadline());
			list.add(summary);
		}
		return list;
	}
	
	/**       流程效率分析                */
	public List<EdocSummary> getEdocSummaryCompleteTimeList(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		StringBuilder sb = new StringBuilder();
		sb.append(" select summary " );
		getQueryHql_2(sb);
		sb.append(" order by summary.runWorkTime,summary.id  ");
		Map<String, Object> parameter = setParameter2Map(accountId,templeteId, workFlowState,
				startDate, endDate);
		return super.find(sb.toString(),-1,-1, parameter);
	}
	
	private Map<String, Object> setParameter2Map(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate) {
		Map<String,Object> parameter = new HashMap<String,Object>();
		parameter.put("templeteId", templeteId);
		parameter.put("state", workFlowState);
		parameter.put("startDate", startDate);
		parameter.put("endDate", endDate);
		parameter.put("orgAccountId", accountId);
		return parameter;
	}
	private Map<String,Integer>  getInfo(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate){
		StringBuilder sb = new StringBuilder();
		sb.append(" select " );
		sb.append(" avg(summary.runWorkTime),");
		sb.append(" count(summary.id)  ");
		getQueryHql(sb);
		Map<String, Object> parameter = setParameter2Map(
				accountId,
				templeteId, 
				workFlowState, 
				startDate, 
				endDate);
		List l = super.find(sb.toString(),-1,-1, parameter);
		Map<String,Integer> map = new HashMap<String,Integer>(); 
		if(Strings.isNotEmpty(l)){
			Object[] obj = (Object[])l.get(0);
			Integer avgRunWorkTime = 0;
			if(obj[0]!=null){
				avgRunWorkTime = ((Number)obj[0]).intValue();
			}
			Integer c = ((Number)obj[1]).intValue();
			map.put("AVG", avgRunWorkTime);
			map.put("COUNT", c);
		}
		return map;
	}
	public Integer getAvgRunWorkTimeByTempleteId(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate){
		
		StringBuilder sb = new StringBuilder();
		sb.append("select summary.createTime,summary.completeTime ,summary.runWorkTime,summary.state ");
		getQueryHql(sb);
		
		Map<String, Object> parameter = setParameter2Map(
				accountId,
				templeteId,
				workFlowState, 
				startDate, 
				endDate);
		
		List<Object[]> l = (List<Object[]>)super.find(sb.toString(), -1,-1,parameter);
		
		Long sumRunWorkTime = 0L;
		Long avgRunWorkTime = 0L;
		if(Strings.isNotEmpty(l)){
			for(Object[] obj : l){
				Date sdate = (Date)obj[0];
				Date edate = (Date)obj[1];
				Long runWrokTime = null;
				if(obj[2]!=null){
					runWrokTime = ((Number)obj[2]).longValue();
				}
				Integer state = 0;
				if(obj[3]!=null){
					state = ((Number)obj[3]).intValue();
				}
				//如果有已经计算出来的运行时长，直接取运行时长
				if(runWrokTime != null){
					sumRunWorkTime += runWrokTime;
					continue;
				}else{
					if(edate == null)
						edate = new Date();
					
					sumRunWorkTime += Functions.getMinutesBetweenDatesByWorkTime(sdate,edate,accountId);
				}
				
			}
			avgRunWorkTime = sumRunWorkTime / l.size();
		}
		return  avgRunWorkTime.intValue();
}
	public Integer getCaseCountByTempleteId (
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate){
		Integer c = getInfo(accountId,templeteId, workFlowState, startDate, endDate).get("COUNT");
		return  c == null ? 0: c;
	}
	private void getQueryHql(StringBuilder sb) {
		//EdocSummary表中待发的数据state字段记录为0，导致统计时将待发数据也统计出来了，这里关联CtpAffair表，将待发数据排除掉
		sb.append(" from EdocSummary as summary , CtpAffair as affair");
		sb.append(" where ");
		sb.append(" summary.templeteId=:templeteId ");
		sb.append(" and summary.state in (:state) ");
		sb.append(" and summary.createTime between :startDate and :endDate ");
		sb.append(" and summary.orgAccountId = :orgAccountId");
		sb.append(" and summary.id = affair.objectId");
		sb.append(" and affair.state =" + StateEnum.col_sent.key());
	}
	
	private void getQueryHql_2(StringBuilder sb) {
		//EdocSummary表中待发的数据state字段记录为0，导致统计时将待发数据也统计出来了，这里关联CtpAffair表，将待发数据排除掉
		sb.append(" from EdocSummary as summary , CtpAffair as affair");
		sb.append(" where ");
		sb.append(" summary.templeteId=:templeteId ");
		sb.append(" and summary.state in (:state) ");
		sb.append(" and summary.completeTime between :startDate and :endDate ");
		sb.append(" and summary.orgAccountId = :orgAccountId");
		sb.append(" and summary.id = affair.objectId");
		sb.append(" and affair.state =" + StateEnum.col_sent.key());
	}
	public Integer  getCaseCountGTSD(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState, Date startDate, Date endDate,Integer standarduration){
		StringBuilder sb = new StringBuilder();
		sb.append(" select " );
		sb.append(" count(summary.id) ");
		getQueryHql(sb);
		sb.append(" and summary.runWorkTime > :standarduration ");
		Map<String, Object> parameter = setParameter2Map(accountId,templeteId, workFlowState, startDate, endDate);
		parameter.put("standarduration", standarduration == null ? 0L: Long.valueOf(standarduration));
		List l = super.find(sb.toString(),-1,-1, parameter);
		if(Strings.isNotEmpty(l)){
			return ((Number)l.get(0)).intValue();
		}
		return 0;
	}
	
	public Double getOverCaseRatioByTempleteId(
			Long accountId,
			Long templeteId,
			List<Integer> workFlowState,
			Date startDate,
			Date endDate){
		
		StringBuilder sb = new StringBuilder();
		sb.append("select summary.createTime,summary.completeTime,summary.deadline,summary.overWorkTime,summary.state");
		getQueryHql(sb);
		
		Map<String, Object> parameter = setParameter2Map(
				accountId,
				templeteId,
				workFlowState, 
				startDate, 
				endDate);
		
		List<Object[]> l = (List<Object[]>)super.find(sb.toString(),-1,-1, parameter);
		
		Integer countAll = 0;
		Integer countOver = 0;
		if(Strings.isNotEmpty(l)){
			for(Object[] obj : l){
				countAll++;
				Date sdate = (Date)obj[0];
				Date edate = (Date)obj[1];
				Long  deadline = 0L;
				if(obj[2]!=null){
					deadline = ((Number)obj[2]).longValue();
				}
				
				//没有设置流程期限就不算超期。
				if(deadline == null || deadline == 0)
					continue;
				
				Long overWorkTime = 0L;
				if(obj[3]!=null){
					overWorkTime  =  ((Number)obj[3]).longValue();
				}
				
				Integer state = 0;
				if(obj[4]!=null){
					state  =  ((Number)obj[4]).intValue();
				}
				if(overWorkTime>0) {
					countOver++;
				}else{
					if(edate == null)
						edate = new Date();
					
					Long run = Functions.getMinutesBetweenDatesByWorkTime(sdate,edate,accountId);
					Long workDeadline = Functions.convert2WorkTime(deadline, accountId);
					if(run>workDeadline){
						countOver++;
					}
				}
			}
		}
		double ratio = 0.0;
		if(countAll!=0){
			ratio = countOver/(countAll*1.0);
		}
		return  ratio;
	}
	
	public void saveSubjectWrapRecord(EdocSubjectWrapRecord subjectWrapRecord){
		super.getHibernateTemplate().save(subjectWrapRecord);
	}
	
	public void deleteSubjectWrapRecord(Long AccountId, Long userId, int listType, int edocType){
		String hql = "delete from EdocSubjectWrapRecord as obj where obj.AccountId=? and obj.userId=? and obj.listType=? and obj.edocType=?";
		super.bulkUpdate(hql, null, AccountId, userId, listType, edocType);
	}
	
	@SuppressWarnings("unchecked")
	public List<EdocSubjectWrapRecord> getCurrentUserSubjectWrapRecord(Long AccountId, Long userId, int listType, int edocType){
		String hql = "from EdocSubjectWrapRecord as obj where obj.AccountId=:AccountId and obj.userId=:userId and obj.listType=:listType and obj.edocType=:edocType";
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("AccountId", AccountId);
		params.put("userId", userId);
		params.put("listType", listType);
		params.put("edocType", edocType);
		List<EdocSubjectWrapRecord> list = (List<EdocSubjectWrapRecord>)super.find(hql,-1,-1, params);
		return list;
	}
	//lijl添加
	public void saveOrUpdateClean(EdocSummary summary)
	{
		this.getHibernateTemplate().clear();
		super.getHibernateTemplate().saveOrUpdate(summary);

	}
	
	
	public List<EdocSummaryModel> getMyEdocDeadlineNotEmpty(Map<String,Object> tempMap){
	    Long currentUserId=(Long) tempMap.get("currentUserID");
		Date beginDate=(Date)tempMap.get("beginDate");
		Date endDate=(Date) tempMap.get("endDate");
		
        //在整个公文设置流程期限和流程节点中  设置了流程期限都要考虑，所以直接在Affair中判断deadlineDate是否为0即可
		StringBuilder hql=new StringBuilder();
		//OA-21790 V5性能-目标管理二级首页默认栏目时间视图高并发时性能较差，300并发时响应时间超过5秒
		//lijl添加expectedProcessTime条件,OA-42474.开发---对外接口
		 //TODO 性能还有优化空间
		hql.append("from EdocSummary s,CtpAffair a where s.id=a.objectId  " +
              "and a.memberId=:memberId and a.deadlineDate is not null and a.receiveTime is not null  " +
              "and a.state in (:state) and a.app in(:app) and a.delete =:delete " +
              "and a.expectedProcessTime between :beginDate and :endDate");
		Map<String,Object> params=new HashMap<String, Object>();
		params.put("delete", false);
		params.put("beginDate", beginDate);
		params.put("endDate", endDate);
        params.put("memberId", currentUserId);
        List<Integer> stateList = new ArrayList<Integer>();
        //OA-32706 时间视图上，已回退的协同和公文不应该再在时间视图上显示
        stateList.add(StateEnum.col_pending.key()); 
        //时间视图上 不显示已发的
//        stateList.add(StateEnum.col_sent.key()); 
        params.put("state", stateList);
        List<Integer> appList = new ArrayList<Integer>();
        appList.add(ApplicationCategoryEnum.edocSend.key());
        appList.add(ApplicationCategoryEnum.edocRec.key());
        appList.add(ApplicationCategoryEnum.edocSign.key());
        params.put("app", appList); 
		
		List list = super.find(hql.toString(),-1,-1, params);
		Calendar cal = Calendar.getInstance();
		EdocRegisterDao registerDao = (EdocRegisterDao)AppContext.getBean("edocRegisterDao");
		
		List<EdocSummaryModel> modelList = new ArrayList<EdocSummaryModel>();
		for(int i=0;i<list.size();i++){
		    Object[] obj = (Object[])list.get(i);
		    EdocSummary summary = (EdocSummary)obj[0];
		    CtpAffair affair = (CtpAffair)obj[1];
		    
		    EdocSummaryModel model = new EdocSummaryModel();
		    model.setSummary(summary);
		    
		    //如果是电子收文，来文单位取登记表中的edocUnit
		    if(summary.getEdocType() == 1){
		        
		        EdocRegister register = registerDao.findRegisterByDistributeEdocId(affair.getObjectId());
		        if(register!=null){
		            String edocUnit = register.getEdocUnit();
		            if(Strings.isNotBlank(edocUnit)){
	                    model.setEdocUnit(edocUnit);
	                }
		            else{
		                model.setEdocUnit(summary.getSendUnit());
		            }
		        }
		        //如果是纸质收文，直接去收文的发文单位    
                  else{
                      model.setEdocUnit(summary.getSendUnit());
                  }
		    }
		    //如果是发文
		    else{
		        model.setEdocUnit(summary.getSendUnit());
		    }
		    
		    model.setAffairId(affair.getId());
		    model.setState(affair.getState());
		    //lijl注销,OA-42474.开发---对外接口
//	        Date receiveTime = affair.getReceiveTime();
//            if(receiveTime != null){
//                Long deadlineDate = affair.getDeadlineDate();
//                
//                if(deadlineDate != null){
//                    cal.setTime(receiveTime);
//                    cal.add(Calendar.MINUTE,Integer.parseInt(String.valueOf(deadlineDate)));   
//                    Date deadline = cal.getTime();
//                    model.setDeadLineDate(deadline);
//                    
//                    cal.add(Calendar.MINUTE, -30);
//                    Date deadlineDisplay = cal.getTime();
//                    model.setDeadLineDisplayDate(deadlineDisplay);
//                }
//            }
		    //lijl添加,OA-42474.开发---对外接口
		    if(affair.getExpectedProcessTime()!=null){
		    	cal.setTime(affair.getExpectedProcessTime());
		    	cal.add(Calendar.MINUTE, -30);
		    	Date deadlineDisplay = cal.getTime();
		    	model.setDeadLineDate(affair.getExpectedProcessTime());
		    	model.setDeadLineDisplayDate(deadlineDisplay);
		    }
		    
		    //model.setDeadLineDisplayDate(affair.getExpectedProcessTime());
	        model.setSubject(affair.getSubject());
	        model.setBodyType(affair.getBodyType());
	        model.setHasAttachments(summary.isHasAttachments());
	        //加上当前公文的状态，比如当公文终止时，时间线要根据这个属性值来显示终止图标
	        model.setEdocStatus(summary.getState());
	        //OA-32715 WAS+Sqlserver：目标管理--时间视图 日视图不显示到期公文
	        //lijl注销,OA-42474.开发---对外接口
//	        if(model.getDeadLineDate().after(beginDate)&&model.getDeadLineDate().before(endDate)){
//	        }
	        modelList.add(model);
	        
		}
		return modelList;
	}
	
	
	public List<WorkflowData> selectWorkflowDataByCondition(String subject, Date beginDate, Date endDate, Map<String,List<Long>> senderMap, 
            int flowstate, int edocType, String operationType, String[] operationTypeIds,long accountId,boolean isPage,FlipInfo fi){
	    return this.selectWorkflowDataByCondition(null, subject, beginDate, endDate, senderMap, flowstate, edocType, operationType,
	    		operationTypeIds, accountId, isPage,fi);
	}
	
	
	public List<WorkflowData> selectWorkflowDataByCondition(FlipInfo flipInfo,String subject, Date beginDate, Date endDate, Map<String,List<Long>> senderMap,
            int flowstate, int edocType, String operationType, String[] operationTypeIds,long accountId,boolean isPage,FlipInfo fi){
	    StringBuilder hql=new StringBuilder();
	    hql.append("select affair.subApp,summary.id,affair.app,summary.subject,m.name,u.name,summary.createTime,summary.processId,summary.caseId, " +
	    		" affair.deadlineDate,affair.remindDate,summary.orgAccountId,affair.finish,summary.templeteId,summary.completeTime,summary.deadlineDatetime,summary.currentNodesInfo " +
	    		", summary.secretLevel from CtpAffair affair,EdocSummary summary,OrgMember m,OrgUnit u ");
	    if(EdocWorkflowManageHandler.TEMPLETE_WORKFLOW.equals(operationType)&&
	    		   (operationTypeIds == null|| operationTypeIds.length==0)){
	    	hql.append(",CtpTemplate t ");
	    }
	    hql.append(" where u.id = summary.orgDepartmentId and m.id=summary.startUserId and summary.id=affair.objectId ");
        hql.append(" and summary.edocType = :edocType");

	    if(EdocWorkflowManageHandler.TEMPLETE_WORKFLOW.equals(operationType)&&
	    		   (operationTypeIds == null || operationTypeIds.length==0)){
	    	hql.append(" and t.id = summary.templeteId and t.system = 1 ");
	    }
		String newGovDocFeature = "";
		if (edocType==0) {
			newGovDocFeature = " and ((affair.app = :affairAppNew and affair.subApp in (:affairSubApp)) or (affair.app = 19)) ";
		} else if(edocType==2) {
			newGovDocFeature = " and ((affair.app = :affairAppNew and affair.subApp in (:affairSubApp)) or (affair.app = 21)) ";
		}else {
			newGovDocFeature = " and (summary.govdocType in (:govdocTypes) or summary.edocType in (1,2)) and summary.transferStatus not in (:excludedTransferStatus) ";
		}
		hql.append(" and summary.state = :state and ( (affair.state = :affairState "+newGovDocFeature+") or ( affair.state = :affairState1 and affair.subState= :subState1 ) ) ");

		if(Strings.isNotBlank(subject)){
	        hql.append(" and summary.subject like :subject ");
	    }
	    if(beginDate != null){
	        hql.append(" and summary.createTime >= :beginTime ");
	    }
	    if(endDate != null){
	        hql.append(" and summary.createTime <= :endTime ");
	    }

	    Map<String,Object> params=new HashMap<String, Object>();

	    if(senderMap != null && senderMap.size()>0){
            String memberWhere = "";
            String memberListFlag = "memberListFlag";
            Iterator<String> it = senderMap.keySet().iterator();
            int i=0;
            while(it.hasNext()){
                String type = it.next();
                List<Long> memberList = senderMap.get(type);
                if(i==0){
                    memberWhere += " and(";
                }else{
                    memberWhere += " or ";
                }
                if("Account".equals(type)){
                    memberWhere += " summary.orgAccountId in (:"+memberListFlag+i+")";
                }else if("Department".equals(type)){
                    memberWhere += " summary.orgDepartmentId in (:"+memberListFlag+i+")";
                }else if("Member".equals(type)){
                    memberWhere += " summary.startUserId in (:"+memberListFlag+i+")";
                }
                params.put(memberListFlag+i, memberList);

                i++;
            }
            memberWhere +=")";
            hql.append(memberWhere);
        }
        //如果发起对象没有选择，那么查整个集团的公文流程
        //那么就不需要再拼接sql


	    //单位类型， 查的自由流程或者 模板流程
	    if(Strings.isNotBlank(operationType)){
	        //如果发起对象没有选择，然而是查整个单位
	        if(senderMap == null || senderMap.size() == 0){
	            hql.append(" and summary.orgAccountId = :accountId ");
	            params.put("accountId", accountId);
	        }

	        //自由流程
	        if(EdocWorkflowManageHandler.COMMON_WORKFLOW.equals(operationType)){
	            hql.append(" and summary.templeteId is null ");
	        }
	        //模板流程
	        else{
	            hql.append(" and summary.templeteId is not null ");
	            if(operationTypeIds != null && operationTypeIds.length > 0){
	                List<Long> templeteList = new ArrayList<Long>();
	                for(String tid : operationTypeIds){
	                    templeteList.add(Long.parseLong(tid));
	                }
	                hql.append(" and summary.templeteId in (:tid) ");
	                params.put("tid", templeteList);
	            }
	        }
	    }
	    //hql.append(" and  summary.case_id is not null and summary.case_id<>0 ");//快速发文的流程过滤掉
        params.put("edocType", edocType);
        params.put("state", flowstate);
        params.put("affairState", 2); //affair.state=2 表示起草人的已发事项， 加上这个确保查询结果不重复
        params.put("affairState1", 1); //affair.state=1
        params.put("subState1", 16); //affair.state=16

		if (edocType==0) {// 参考发文登记薄的条件
			params.put("affairAppNew", com.seeyon.ctp.common.constants.ApplicationCategoryEnum.edoc.key());
			List<Integer> affairSubApp = new ArrayList<Integer>();
			affairSubApp.add(ApplicationSubCategoryEnum.edoc_fawen.key());
			affairSubApp.add(ApplicationCategoryEnum.edocSend.getKey());
			params.put("affairSubApp", affairSubApp);
		} else if (edocType==2) {// 签报
			params.put("affairAppNew", com.seeyon.ctp.common.constants.ApplicationCategoryEnum.edoc.key());
			List<Integer> affairSubApp = new ArrayList<Integer>();
			affairSubApp.add(ApplicationSubCategoryEnum.edoc_qianbao.key());
			affairSubApp.add(ApplicationCategoryEnum.edocSign.getKey());
			params.put("affairSubApp", affairSubApp);
		} else{// 参考收文登记簿的显示条件（交换流程分办后会产生对应的收文流程，此两个流程只显示一个即可）
			List<Integer> govdocTypes = new ArrayList<Integer>();
			govdocTypes.add(ApplicationSubCategoryEnum.edoc_shouwen.getKey());
			govdocTypes.add(ApplicationSubCategoryEnum.edoc_jiaohuan.getKey());
			//govdocTypes.add(ApplicationSubCategoryEnum.getKey());
			params.put("govdocTypes",govdocTypes);
			List<Integer> excludedTransferStatus = new ArrayList<Integer>();
			//excludedTransferStatus.add(GovdocEnum.TransferStatus.defaultStatus.getKey());
			excludedTransferStatus.add(GovdocEnum.TransferStatus.receiveSignedFenban.getKey());
			//excludedTransferStatus.add(GovdocEnum.TransferStatus.waitSigned.getKey());查询收文流程也要把交换流程查出来。
			params.put("excludedTransferStatus",excludedTransferStatus);
		}

        if(Strings.isNotBlank(subject)){
            params.put("subject","%"+subject+"%");
        }
        if(beginDate != null){
            params.put("beginTime",beginDate);
        }
        if(endDate != null){
            params.put("endTime",endDate);
        }
        hql.append(" order by summary.createTime desc ");

        List<WorkflowData> flowList = new ArrayList<WorkflowData>();

        List list = null;
        if(flipInfo != null){
        	list = DBAgent.find(hql.toString(), params, flipInfo);
        }else{
        	if(isPage){
        		list = super.find(hql.toString(), params,fi);
	        }else{
	            list = super.find(hql.toString(),-1,-1, params);
	        }
        }

        for(int i=0;i<list.size();i++){
            Object[] obj = (Object[])list.get(i);
            WorkflowData flow = new WorkflowData();
            int j=0;
            int sun_app = -1;
            Object sub_app_obj = obj[j++];
            if(null != sub_app_obj){
            	sun_app =(Integer)sub_app_obj;
            }
            flow.setEdocType(-1);
            flow.setSummaryId(String.valueOf(obj[j++]));
            //应用类型
            int app = Integer.parseInt(String.valueOf(obj[j++]));
            String appStr = "";
            if(app == ApplicationCategoryEnum.edocSend.key()){ //TODO 增加一个判断新公文 和老公文的区别，同时区分收文和发文
                appStr = ResourceUtil.getString("edoc.docmark.inner.send");
            }else if(app==ApplicationCategoryEnum.edocRec.key()){
                appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            }else{
                appStr = ResourceUtil.getString("edoc.docmark.inner.signandreport");
            }

//            flow.setAppType(String.valueOf(obj[j++]));
            flow.setSubject(String.valueOf(obj[j++]));
            flow.setInitiator(String.valueOf(obj[j++]));
            flow.setDepName(String.valueOf(obj[j++]));
            flow.setSendTime(Timestamp.valueOf(String.valueOf(obj[j++])));
            flow.setProcessId(String.valueOf(obj[j++]));
            flow.setCaseId((Long)(obj[j++]));
            if(obj[j] !=null){
                Long deadline = (Long)(obj[j]);
                flow.setDeadLine(deadline);
            }
            j++;
            flow.setAdvanceRemind((Long)(obj[j++]));
            flow.setAccountId((Long)(obj[j++]));
//            long end = (Long)(obj[j++]);
//            flow.setEndFlag((int)end);
            j++;
            Long templeteId = (Long)(obj[j++]);
            if(templeteId!= null){
                flow.setIsFromTemplete(true) ;
                flow.setTempleteId(templeteId);
            }
            if(edocType == 1){
                flow.setAppEnumStr(ApplicationCategoryEnum.edocRec.name());
            }else if(edocType == 0){
                flow.setAppEnumStr(ApplicationCategoryEnum.edocSend.name());
            }else if(edocType == 2){
                flow.setAppEnumStr(ApplicationCategoryEnum.edocSign.name());
            }
            if(app == ApplicationCategoryEnum.edoc.key() && sun_app != -1){
            	if(sun_app == ApplicationSubCategoryEnum.edoc_fawen.key()){//發文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.send");
            		flow.setAppEnumStr(com.seeyon.ctp.common.constants.ApplicationCategoryEnum.govdocSend.name());
            		flow.setEdocType(FormType.govDocSendForm.getKey());
            	}else if(sun_app == ApplicationSubCategoryEnum.edoc_shouwen.key()){//收文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            		flow.setAppEnumStr(com.seeyon.ctp.common.constants.ApplicationCategoryEnum.govdocRec.name());
            		flow.setEdocType(FormType.govDocReceiveForm.getKey());
            	}else if(sun_app == ApplicationSubCategoryEnum.edoc_qianbao.key()){//签报
            		appStr = ResourceUtil.getString("edoc.docmark.inner.signandreport");
            		flow.setAppEnumStr(com.seeyon.ctp.common.constants.ApplicationCategoryEnum.govdocSign.name());
            		flow.setEdocType(FormType.govDocSignForm.getKey());
            	}else{
            		appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            		flow.setAppEnumStr(com.seeyon.ctp.common.constants.ApplicationCategoryEnum.govdocRec.name());
            		flow.setEdocType(FormType.govDocExchangeForm.getKey());
            	}
            }
            flow.setAppType(appStr);
            EdocSummary summary = new EdocSummary();
            String completeTime = String.valueOf(obj[j++]);
            if(Strings.isNotBlank(completeTime)&& !"null".equals(completeTime)){
            	summary.setCompleteTime(Timestamp.valueOf(completeTime));
            }
            java.util.Date deadlineDatetime=(java.util.Date)obj[j++];
            //设置流程期限显示内容
            flow.setDeadlineDatetimeName(WFComponentUtil.getDeadLineName(deadlineDatetime));
            String currentNodesInfo = (String)(obj[j++]);
            if(Strings.isNotBlank(currentNodesInfo) && currentNodesInfo.indexOf(";;") != -1){
            	currentNodesInfo = currentNodesInfo.replaceAll(";;", ";");
            }
            summary.setCurrentNodesInfo(currentNodesInfo);
            flow.setCurrentNodesInfo(EdocHelper.parseCurrentNodesInfo(summary));
            flow.setSecretLevel(String.valueOf((obj[j++])));
            flowList.add(flow);
        }
	    return flowList;
	}
	
	
	public List checkSubject4Personal(String categoryId,String subject,long userId ){
        String hql = "select t.id from  CtpTemplate as t where t.categoryId= :categoryId and t.subject= :subject and t.memberId = :memberId and t.system =:system and t.delete =:isDelete";
        Map map = new HashMap();
        map.put("categoryId",Long.parseLong(categoryId));
        map.put("subject",subject.trim());
        map.put("memberId",userId);
        map.put("system",Boolean.FALSE);
        map.put("isDelete", Boolean.FALSE);
        return find(hql,-1,-1, map);
	    
	}
	
	
	public List checkSubject4System(String categoryId,String subject,long orgAccountId ){
	    //OA-23264  单位管理员-公文应用设置，删除名称为A的模板后，再在当前分类下新建名称为A的模板，便提示"该模板已存在"  
        String hql = "select id from  CtpTemplate template where categoryId= :categoryId and subject= :subject " +
        		"and orgAccountId = :orgAccountId and system =:system and template.delete = :delete ";
        Map map = new HashMap();
        map.put("categoryId",Long.parseLong(categoryId));
        map.put("subject",subject.trim());
        map.put("orgAccountId",orgAccountId);
        map.put("system",Boolean.TRUE);
        map.put("delete",Boolean.FALSE);  
        List list = find(hql,-1,-1, map);
        return list;
        
    }
	
    public void saveEdocRegisterCondition(EdocRegisterCondition condition) {
        super.save(condition);
    }

    public List<EdocRegisterCondition> getEdocRegisterCondition(long accountId, Map<String, Object> paramMap, User user) {
        
        Map<String,Object> parameter = new HashMap<String,Object>();
        StringBuilder hql = new StringBuilder("from EdocRegisterCondition where accountId=:accountId and type=:type ");
        
        //不是单位管理员需要保存推送的范围
        try {
            if(!GovdocRoleHelper.isAccountExchange(user.getId())){
                String departmentIds = GovdocRoleHelper.getUserExchangeDepartmentIds();
                if (Strings.isNotBlank(departmentIds)) {
                    String[] depIds = departmentIds.split("[,]");
                    if (depIds.length > 0) {
                        
                        hql.append(" and ((contentExt2 like :depts_0) ");
                        parameter.put("depts_0", "%" + depIds[0] +"%");
                        for (int i = 1; i < depIds.length; i++) {
                            hql.append(" or (contentExt2 like :depts_" + i + ")");
                            parameter.put("depts_" + i, "%" + depIds[i] +"%");
                        }
                        hql.append(") ");
                    }
                }
            }
        } catch (BusinessException e) {
            logger.error("收发文登记薄栏目标题权限控制失败", e);
        }
        
        //区分是发文或收文登记簿
        int type = Integer.parseInt(String.valueOf(paramMap.get("type")));
        parameter.put("type", type);
        String subject = "";
        if(paramMap.get("subject") != null){
            subject = String.valueOf(paramMap.get("subject"));
        }       
        if(Strings.isNotBlank(subject)){
            hql.append(" and title like :subject ");
            parameter.put("subject", "%"+SQLWildcardUtil.escape(subject)+"%");
        }
        hql.append(" order by createTime desc ");
        
        int count = -1;
        if(paramMap.get("count") != null){
            count = Integer.parseInt(String.valueOf(paramMap.get("count")));
        }
        parameter.put("accountId", accountId);
        List<EdocRegisterCondition> list = null;
        if(count == -1){
            list = super.find(hql.toString(),parameter);
        }else{
            list = super.find(hql.toString(),0,count,parameter);
        }
        return list;
    }

    public int getEdocRegisterConditionTotal(long accountId,int type,String subject) {
        Map<String,Object> parameter = new HashMap<String,Object>();
        String hql = " select title from EdocRegisterCondition where accountId=:accountId and type=:type ";       
        parameter.put("accountId", accountId);
        parameter.put("type", type);
        if(Strings.isNotBlank(subject)){
            hql += " and title like :subject ";
            parameter.put("subject", "%"+subject+"%");
        }
        
        List list = super.find(hql, -1,-1, parameter);
        int total = list.size();
        return total;
    }

    public void delEdocRegisterCondition(long id) {
        super.delete(EdocRegisterCondition.class, id);
    }

    public EdocRegisterCondition getEdocRegisterConditionById(long id) {
        String hql = "from EdocRegisterCondition where id = :id";
        Map parameter = new HashMap();
        parameter.put("id", id);
        List<EdocRegisterCondition> list = super.find(hql,-1,-1, parameter);
        return list.get(0);
    }
	
    @SuppressWarnings("unchecked")
	public List<Long> findIndexResumeIDList(final Date starDate,final Date endDate,final Integer firstRow,final Integer pageSize)
	    {
		return (List<Long>) getHibernateTemplate().execute(new HibernateCallback() {
		    public Object doInHibernate(Session session) throws HibernateException, SQLException {
		        StringBuilder hql = new StringBuilder("select b.id  from EdocSummary as b where b.state in (0,1,3) and b.createTime >= ? and b.createTime <= ?"); 
		        hql.append(" order by b.createTime desc");
		        Query query = null;     
		        query = session.createQuery(hql.toString());
		        query.setParameter(0, starDate);
		        query.setParameter(1, endDate);
		       // query.setParameter(2, MainbodyType.FORM.getKey());
		        query.setFirstResult(firstRow);
		        query.setMaxResults(pageSize);
		        return query.list();
		    }
		});
	}
    
    public boolean isBeSended(Long summaryId) {
		//String hqlStr="from EdocSendDetail e,EdocSendRecord r where e.sendRecordId=r.id and e.status = 0 and r.edocId = ? ";
    	String hqlStr="from EdocSendRecord r where r.status != 0 and r.edocId = ?";
//        Map<String,Object> columns = new HashMap<String,Object>(1);
//        columns.put("edocId", summaryId);
        Long[] values = {summaryId};
        Type[] types = {Hibernate.LONG};
		int queryCount = super.getQueryCount(hqlStr, values, types);
	   if (queryCount > 0) {
			return true;
		} else {
			return false;
        }
	}
    
    /**
     * 更新流程超期字段
     * @param edocId
     * @param isCoverTime
     */
	public void updateEdocSummaryCoverTime(Long edocId, boolean isCoverTime) {
		String hsql = "update EdocSummary set coverTime=:isCoverTime where id=:edocId";
		java.util.Map<String, Object> params = new HashMap<String, Object>();
		params.put("isCoverTime", isCoverTime);
		params.put("edocId", edocId);
		super.bulkUpdate(hsql, params);
	}

    public void transSetFinishedFlag(EdocSummary summary) {
        StringBuilder sb  = new StringBuilder();
        sb.append("UPDATE " + EdocSummary.class.getCanonicalName() + " set completeTime=? , ");
        sb.append("overTime = ?, overWorkTime = ?,");
        sb.append("runTime = ? ,runWorkTime = ?, state = ? ");
        sb.append("where id=? ");
        super.bulkUpdate(sb.toString(), null, 
                summary.getCompleteTime()==null?new Timestamp(System.currentTimeMillis()):summary.getCompleteTime(), 
                summary.getOverTime() == null? 0:summary.getOverTime(),
                summary.getOverWorkTime() == null ? 0:summary.getOverWorkTime(),
                summary.getRunTime() == null ? 0: summary.getRunTime(),
                summary.getRunWorkTime() == null ?0 :summary.getRunWorkTime(),
                summary.getState(),summary.getId());
    }
    
    
    /**
     * 收文登记时来文文号若有重复需要给出提醒
     * @param docMark
     * @return
     */
    public int checkDocMarkIsUsedByRec(int edocType,String docMark,long accountId){
    	int count = 0;
        String summaryHql = "select summary.id  from EdocSummary summary where " +
        		" summary.edocType = :edocType and summary.docMark = :docMark " +
        		"and summary.orgAccountId = :accountId";
        Map<String, Object> summaryParams = new HashMap<String, Object>();
        summaryParams.put("edocType", edocType);
        summaryParams.put("docMark", docMark);
        summaryParams.put("accountId", accountId);
        long startTime = System.currentTimeMillis();
        log.info("summaryStatTime:"+startTime);
        List<Long> summaryIds = super.find(summaryHql, -1, -1,summaryParams);
        log.info("查询summary使用时间:"+(System.currentTimeMillis()-startTime));
        if(Strings.isNotEmpty(summaryIds)){
        	String affairHql = "select count(affair.id) from CtpAffair affair where affair.state = :state and affair.objectId in (:objectId)";
        	Map<String, Object> affairParams = new HashMap<String, Object>();
        	affairParams.put("state", 2);//公文已发
        	affairParams.put("objectId", summaryIds);
        	long affairStartTime = System.currentTimeMillis();
        	log.info("affairStartTime:"+affairStartTime);
        	count = super.count(affairHql, affairParams);
        	log.info("查询affair使用时间:"+(System.currentTimeMillis()-affairStartTime));
        }
        return count;
    }
    
    public List<EdocSummary> findEdocSummarysByIds(List<Long> ids){
    	String hql = "from EdocSummary where id in(:ids)";
    	Map<String, Object> params = new HashMap<String, Object>();
    	params.put("ids", ids);
    	List<EdocSummary> list = super.find(hql,-1,-1, params);
        return list;
    }
    

    
    
    public List<SimpleEdocSummary> findSimpleEdocSummarysByIds(List<Long> ids){
    	
        List<SimpleEdocSummary> simpleEdocSummarys= new ArrayList<SimpleEdocSummary>();
        
        if(Strings.isNotEmpty(ids)){
            StringBuilder selectFields = new StringBuilder();
            selectFields.append( " select ");
            selectFields.append( " id, ");
            selectFields.append( " completeTime, ");
            selectFields.append( " currentNodesInfo, ");
            selectFields.append( " caseId, ");
            selectFields.append( " processId, ");
            selectFields.append( " templeteId, ");
            selectFields.append( " deadlineDatetime, ");
            selectFields.append( " coverTime, ");
            selectFields.append( " identifier ");
            
            String hql = selectFields.toString()+" from EdocSummary where id in(:ids) ";
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("ids", ids);
            List<Object[]> list = super.find(hql,-1,-1, params);
            
            if(Strings.isNotEmpty(list)){
                for(Object[] object : list){
                    SimpleEdocSummary s = new SimpleEdocSummary();
                    
                    s.setId(((Number)object[0]).longValue());
                    s.setCompleteTime(object[1] == null ? null:(Timestamp)object[1]);
                    s.setCurrentNodesInfo((String)object[2]);
                    s.setCaseId(object[3] == null ? null : ((Number)object[3]).longValue());
                    s.setProcessId((String)object[4]);
                    s.setTempleteId(object[5] == null ? null:((Number)object[5]).longValue());
                    s.setDeadlineDatetime(object[6] == null ? null : (Date)object[6]);
                    s.setCoverTime((Boolean)object[7]);
                    s.setIdentifier((String)object[8]);
                    simpleEdocSummarys.add(s);
                }
            }
        }
        
        return simpleEdocSummarys;
    }
    /**
	 * 根据内部文号判断文号内部文号是否已经使用
	 * @param SUMMARYID  公文ID
	 * @param serialNo   内部文号
	 * @param loginAccount  登录单位
	 * @return (1：存在  0：不存在)
	 */
	public int checkRegisterSerialNoExsit(Long objectId,String serialNo,long orgAccountId){
		if(serialNo==null||"".equals(serialNo))return 0;//为空，或者表单中没有这个字段。
		StringBuilder sb=new StringBuilder();
		sb.append("from EdocRegister as register where register.serialNo = ?  and register.orgAccountId=? ");
		List<Object> params = new ArrayList<Object>();
		params.add(SQLWildcardUtil.escape(serialNo));
		params.add(orgAccountId);
		if(objectId != null && objectId.longValue()!=-1) {
			sb.append(" and register.id != ?");
			params.add(objectId);
		}
	    List<EdocRegister> register = super.findVarargs(sb.toString(), params.toArray());
		if(register.size()>0) {
		    return 1;//找到
		}else{			 //没找到
			return 0;	
		}
	}
	
	@Override
	public void update(Object o) {
	    
	    try {
            super.update(o);
        }catch(org.springframework.dao.DuplicateKeyException e){
            //模块解耦,更新通过CAP转换的对象时由于内存中对象不一致,更新不成功.捕获异常,用merge更新.
            getHibernateTemplate().merge(o);
        } catch (org.hibernate.NonUniqueObjectException e) {
            //模块解耦,更新通过CAP转换的对象时由于内存中对象不一致,更新不成功.捕获异常,用merge更新.
            getHibernateTemplate().merge(o);
        }
	}
	
	public void deleteSummaryAndAffair(Long summaryId) {
		Map  pMap = new HashMap();
		pMap.put("id", summaryId);
		String hql = "delete from EdocSummary where id= :id";
		DBAgent.bulkUpdate(hql,pMap);
		pMap.put("isDelete", true);
		hql = "update CtpAffair c set c.delete=:isDelete where c.objectId= :id";
		DBAgent.bulkUpdate(hql,pMap);
	}
	
	public void deleteSummaryByFormRecordId(Long formRecordid){
		Map  pMap = new HashMap();
		pMap.put("formRecordid", formRecordid);
		List<Long> ids = find("select id from EdocSummary where formRecordid= :formRecordid", pMap);		
		if(!ids.isEmpty()){
			String hql = "delete from EdocSummary where formRecordid= :formRecordid";
			DBAgent.bulkUpdate(hql,pMap);
			pMap.clear();
			pMap.put("ids", ids);
			hql = "delete from CtpAffair c where c.objectId in (:ids)";
			DBAgent.bulkUpdate(hql,pMap);
		}
	}
	
	public void updateEdocSummaryArchiveId(Long edocId, Long archiveId) {
		String hsql = "update EdocSummary set archiveId=:archiveId where id=:edocId";
		java.util.Map<String, Object> params = new HashMap<String, Object>();
		params.put("archiveId", archiveId);
		params.put("edocId", edocId);
		super.bulkUpdate(hsql, params);
	}
}
