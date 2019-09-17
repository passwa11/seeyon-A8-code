package com.seeyon.apps.govdoc.dao;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.CTPHibernateDaoSupport;

import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocProcessTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.apps.govdoc.helper.GovdocStatHelper;
import com.seeyon.apps.govdoc.helper.GovdocSwitchHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.GlobalNames;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.dao.paginate.Pagination;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.JDBCAgent;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.common.web.login.CurrentUser;
import com.seeyon.v3x.edoc.domain.EdocMarkDefinition;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.util.EdocStatEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocFinishStateEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocOverTimeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatEnum.GovdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.GovdocStatUtil;
import com.seeyon.v3x.edoc.webmodel.GovdocStatConditionVO;

public class GovdocStatDao  extends BaseHibernateDao<EdocStat>{
	private static final String selectStatSentResultSummary = "summary.id,summary.templeteId,summary.edocType," +
	        "summary.subject,summary.sendDepartmentId,summary.state,summary.startTime, summary.orgDepartmentId, summary.startUserId, summary.orgAccountId" ;
	
	private static final String selectStatResult = "stat.edocType,stat.flowState,stat.createDate,affair.objectId,affair.subject,affair.memberId,affair.app,affair.subApp,affair.nodePolicy,affair.state,affair.subState, count(affair.state) as countAffairState,count(affair.subState) as countAffairSubState";
	
	private static String selectStatListSummary = "summary.id, summary.govdocType, summary.subject, summary.docMark, summary.serialNo, summary.state, summary.startUserId, summary.deadlineDatetime, summary.deadline, summary.hasArchive, summary.sendDepartment,summary.unitLevel,summary.issuer, summary.coverTime, summary.orgAccountId, summary.startTime, summary.archiveId, summary.templeteId, summary.signingDate, summary.completeTime,  summary.registrationDate,summary.bodyType";	
	
	/*************************************************** 公文统计Hql(备用方法) end ***************************************************************/
	/*************************************************** 工作统计  start***************************************************************/
	@SuppressWarnings("unchecked")
	public List<Object[]> findMtechSignCountNum(Date startDate, Date endDate, Long recOrgId) throws Exception{
		String sql = "select detail.id,"
				+ "detail.recOrgId, detail.recOrgName,main.startTime,detail.recTime,detail.status "
				+ "from GovdocExchangeMain main ,GovdocExchangeDetail detail,"
				+ "EdocSummary es"
				+ " where detail.summaryId=es.id  and detail.mainId= main.id and es.state != '4' " 
				+ " and detail.recOrgId ='" + recOrgId+"'"
				+ " and detail.status in(1,2,3,4,11,12,13) " 
				+ " and main.startTime is not null " 
				+ " and main.startTime >= :startDatestamp and main.startTime <= :endDatestamp"
				+ " and detail.sendAccountId='" + CurrentUser.get().getLoginAccount()+"'";
		Timestamp startDatestamp = new Timestamp(startDate.getTime());
		Timestamp endDatestamp = new Timestamp(endDate.getTime());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("startDatestamp", startDatestamp);
		parameterMap.put("endDatestamp", endDatestamp);
		List<Object[]> list = DBAgent.find(sql,parameterMap);
		return list;
	}
	
	/**
	 * 所有未签收
	 * @param startDate
	 * @param endDate
	 * @param recOrgId
	 * @return
	 * @throws Excetption
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findMtechNoRecSignCountNum(Date startDate, Date endDate, Long recOrgId) throws Exception{
		String sql = "select detail.id,"
				+ "detail.recOrgId, detail.recOrgName,main.startTime,detail.recTime "
				+ "from GovdocExchangeMain main ,GovdocExchangeDetail detail,"
				+ "EdocSummary es "
				+ " where detail.summaryId=es.id  and detail.mainId= main.id and es.state != '4' " 
				+ " and detail.recOrgId ='" + recOrgId+"'"
				+ " and main.startTime is not null "
				+ " and detail.status = 1 " 
				+ " and main.startTime >= :startDatestamp and main.startTime <= :endDatestamp"
				+ " and detail.sendAccountId='" + CurrentUser.get().getLoginAccount()+"'";
		Timestamp startDatestamp = new Timestamp(startDate.getTime());
		Timestamp endDatestamp = new Timestamp(endDate.getTime());
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("startDatestamp", startDatestamp);
		parameterMap.put("endDatestamp", endDatestamp);
		List<Object[]> list = DBAgent.find(sql,parameterMap);
		return list;
	}
	
	@SuppressWarnings("unchecked")
	public List<Object[]> findGovdocStatSendBackCount(GovdocStatConditionVO conditionVo) {

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select detail.recOrgId, log.id");
		buffer.append(" from GovdocExchangeMain main");
		buffer.append(",GovdocExchangeDetail detail");
		buffer.append(",GovdocExchangeDetailLog log");
		buffer.append(",EdocSummary summary");
		buffer.append(" where summary.id=main.summaryId and detail.mainId=main.id and detail.id = log.detailId");
		buffer.append(" and summary.state in (0,1,2,3)");
		buffer.append(" and main.type <> 1");
		buffer.append(" and ((summary.govdocType!=0) or (summary.govdocType=0 and summary.state in (0,1,3)))");
		buffer.append(" and log.status = 10");
		buffer.append(" and main.startTime is not null");
		
		//buffer.append(" and detail.sendAccountId = :orgAccountId");
		//parameterMap.put("orgAccountId", String.valueOf(conditionVo.getStatRangeId()));
		if("v3x_edoc_sign_count".equals(conditionVo.getStatType())){//只有本单位发文各单位/部门签收情况才会带发送方条件
			buffer.append(" and summary.orgAccountId = :orgAccountId");
			parameterMap.put("orgAccountId", conditionVo.getStatRangeId());
		}
		
		List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
		if(Strings.isNotEmpty(statRangeList)) {
			buffer.append(" and (");
			for(int i=0; i<statRangeList.size(); i++) {
				if(i != 0) {
					buffer.append(" or");
				}
				buffer.append(" detail.recOrgId = :recOrgId" + i);
				parameterMap.put("recOrgId" + i, statRangeList.get(i));
			}
			buffer.append(")");
		}
		
		if(conditionVo.getStartDate() != null) {
			buffer.append(" and main.startTime >= :startDate");
			parameterMap.put("startDate", conditionVo.getStartDate());
		}
		if(conditionVo.getEndDate() != null) {
			buffer.append(" and main.startTime <= :endDate");
			parameterMap.put("endDate", conditionVo.getEndDate());
		}
		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap);
	
	}
	
	/**
	 * 签收统计穿透
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
	public List<Object[]> signStatToListGovdoc(FlipInfo flipInfo, int listType, GovdocStatConditionVO conditionVo) throws Exception {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		StringBuilder whereStr = new StringBuilder();
		List<Object> params = new ArrayList<Object>();
		if (listType == 1) {
			List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
			if(Strings.isNotEmpty(statRangeList)) {
				whereStr.append(" and (");
				
				for(int i=0; i<statRangeList.size(); i++) {
					if(i != 0) {
						whereStr.append(" or");
					}
					whereStr.append(" detail.rec_Org_Id = ?");
					params.add(statRangeList.get(i));
				}
				whereStr.append(")");
			}
			if(conditionVo.getStartDate() != null) {
				whereStr.append(" and main.start_Time >= ?");
				params.add(Datetimes.getTodayFirstTime(conditionVo.getStartDate()));
			}
			if(conditionVo.getEndDate() != null) {
				whereStr.append(" and main.start_Time <= ?");
				params.add(Datetimes.getTodayLastTime(conditionVo.getEndDate()));
			}
			params.addAll(params);			
			StringBuilder bufferSql = new StringBuilder();
			String hqlStr = "select summary.id, summary.docMark,summary.subject,summary.issuer"
					+ ",summary.sendUnit,summary.sendDepartment,summary.startTime from EdocSummary summary where summary.id in(:ids)";
			bufferSql.append("select * from (SELECT log.back_summary_id as id,main.start_time FROM   ");
			bufferSql.append("	Govdoc_Exchange_Main main,                                     ");
			bufferSql.append("	Govdoc_Exchange_Detail detail,                                 ");
			bufferSql.append("	Govdoc_Exchange_Detail_Log log                                 ");
			bufferSql.append("	WHERE                                                          ");
			bufferSql.append("	 detail.main_Id = main.id                                      ");
			bufferSql.append("	AND main.EXCHANGE_TYPE <> 1                                             ");
			bufferSql.append("	AND detail.id = log.detail_Id                                  ");
			bufferSql.append("	AND log. STATUS = 10                                           ");
			bufferSql.append("	AND log.back_Summary_Id IS NOT NULL                            ");
			bufferSql.append("	AND main.start_Time IS NOT NULL                                ");
			bufferSql.append(whereStr);
			bufferSql.append("	union                                                          ");
			bufferSql.append("	SELECT detail.summary_Id as id,main.start_time FROM            ");
			bufferSql.append("		Govdoc_Exchange_Main main,                                 ");
			bufferSql.append("		Govdoc_Exchange_Detail detail                              ");
			bufferSql.append("	WHERE                                                          ");
			bufferSql.append("	detail.main_Id = main.id                                       ");
			bufferSql.append("	AND main.EXCHANGE_TYPE <> 1                                             ");
			bufferSql.append("	AND detail. STATUS IN (1, 2, 3, 5, 12, 13, 15)                 ");
			bufferSql.append("	AND main.start_Time IS NOT NULL                                ");
			bufferSql.append(whereStr);
			bufferSql.append(") t	order by t.start_Time desc                                 ");
			JDBCAgent jdbcAgent=new JDBCAgent();
			try{
				jdbcAgent.findByPaging(bufferSql.toString(),params, flipInfo);	
			} finally{
				jdbcAgent.close();
			}		
			List<Map<String,Object>> maplist = flipInfo.getData();
			List<Long> summaryIdlist = new ArrayList<Long>();
			for (Map<String, Object> map : maplist) {
				summaryIdlist.add(Long.parseLong(map.get("id").toString()));
			}
			if(CollectionUtils.isEmpty(summaryIdlist)){
				return null;
			}
			Map<String,Object> paramsmap = new HashMap<String, Object>();
			paramsmap.put("ids", summaryIdlist);
			hqlStr = hqlStr.concat(" order by summary.startTime desc ");
			return (List<Object[]>)DBAgent.find(hqlStr, paramsmap);
		}else{
			 
			String distinct = "distinct";
			String status = "detail.status";
			if (listType==6) {
				status = "log.status";
				buffer.append("select " + distinct + " main.summaryId, detail.recOrgId, main.startTime, detail.recTime, " + status + ",detail.recUserName,log.backSummaryId");
			}else {
				buffer.append("select " + distinct + " summary.id, detail.recOrgId, main.startTime, detail.recTime, " + status + ",detail.recUserName,detail.summaryId");
			}
			if (listType==6) {
				buffer.append(" ,log.time,log.backOpinion");
			}
			buffer.append(" from GovdocExchangeMain main");
			buffer.append(",GovdocExchangeDetail detail");
			if (listType==6) {
				buffer.append(",GovdocExchangeDetailLog log");
			}else{
				buffer.append(",EdocSummary summary");
			}
			buffer.append(" where ");
			buffer.append("  detail.mainId=main.id");
			buffer.append(" and main.type <> 1");
			if (listType==6) {
				buffer.append(" and detail.id = log.detailId");
			}else {
				buffer.append(" and summary.id =detail.summaryId ");
			}
			if (listType==6) {
				buffer.append(" and log.status = 10");
				buffer.append(" and log.backSummaryId is not null");
			}else{
				buffer.append(" and detail.status in (1,2,3,5,12,13,15)");
			}
			buffer.append(" and main.startTime is not null");
			
			if("v3x_edoc_sign_count".equals(conditionVo.getStatType())&&conditionVo.getStatRangeId()!=null){//只有本单位发文各单位/部门签收情况才会带发送方条件
				buffer.append(" and detail.sendAccountId = :orgAccountId");
				parameterMap.put("orgAccountId", conditionVo.getStatRangeId());
			}
			
			List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
			if(Strings.isNotEmpty(statRangeList)) {
				buffer.append(" and (");
				for(int i=0; i<statRangeList.size(); i++) {
					if(i != 0) {
						buffer.append(" or");
					}
					buffer.append(" detail.recOrgId = :recOrgId" + i);
					parameterMap.put("recOrgId" + i, statRangeList.get(i));
				}
				buffer.append(")");
			}
			
			if(conditionVo.getStartDate() != null) {
				buffer.append(" and main.startTime >= :startDate");
				parameterMap.put("startDate", conditionVo.getStartDate());
			}
			if(conditionVo.getEndDate() != null) {
				buffer.append(" and main.startTime <= :endDate");
				parameterMap.put("endDate", conditionVo.getEndDate());
			}
		}
		
		if(listType ==1){
			buffer.append(" order by summary.startTime desc");
		}else if(listType ==5 || listType==6){
			buffer.append(" order by main.startTime desc");
		}else{
			buffer.append(" order by detail.recTime desc");	
		}
		                   
		
 		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap);
	}
	
	public Integer findGovdocStatBackCount(int key, GovdocStatConditionVO conditionVo) throws Exception {

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select distinct log.id");
		buffer.append(" from GovdocExchangeMain main");
		buffer.append(",GovdocExchangeDetail detail");
		buffer.append(",GovdocExchangeDetailLog log");
		buffer.append(" where  detail.mainId=main.id and log.detailId = detail.id");
		buffer.append(" and main.type <> 1");
		buffer.append(" and log.status = 10");
		buffer.append(" and main.startTime is not null");
		buffer.append(" and log.backSummaryId is not null");
		
		//buffer.append(" and detail.sendAccountId = :orgAccountId");
		//parameterMap.put("orgAccountId", String.valueOf(conditionVo.getStatRangeId()));
//		if("v3x_edoc_sign_count".equals(conditionVo.getStatType())){//只有本单位发文各单位/部门签收情况才会带发送方条件
//			buffer.append(" and summary.orgAccountId = :orgAccountId");
//			parameterMap.put("orgAccountId", conditionVo.getStatRangeId());
//		}
		
		List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
		if(Strings.isNotEmpty(statRangeList)) {
			buffer.append(" and (");
			for(int i=0; i<statRangeList.size(); i++) {
				if(i != 0) {
					buffer.append(" or");
				}
				buffer.append(" detail.recOrgId = :recOrgId" + i);
				parameterMap.put("recOrgId" + i, statRangeList.get(i));
			}
			buffer.append(")");
		}
		
		if(conditionVo.getStartDate() != null) {
			buffer.append(" and main.startTime >= :startDate");
			parameterMap.put("startDate", conditionVo.getStartDate());
		}
		if(conditionVo.getEndDate() != null) {
			buffer.append(" and main.startTime <= :endDate");
			parameterMap.put("endDate", conditionVo.getEndDate());
		}
		return DBAgent.count(buffer.toString(), parameterMap);
	
	}
	
	/**
	 * 新公文签收统计
	 * @param listType
	 * @param conditionVo
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findGovdocStatSignResult(int listType,GovdocStatConditionVO conditionVo) throws Exception {

		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		String str = " main.summaryId = summary.id ";
		if("v3x_edoc_sign_self_count".equals(conditionVo.getStatType())){
			str = " detail.summaryId=summary.id ";
		}
		StringBuilder buffer = new StringBuilder();
		buffer.append("select summary.id, detail.recOrgId, main.startTime, detail.recTime, detail.status,detail.summaryId");
		buffer.append(" from GovdocExchangeMain main");
		buffer.append(",GovdocExchangeDetail detail");
		buffer.append(",EdocSummary summary");
		buffer.append(" where " +str +" and detail.mainId=main.id");
		buffer.append(" and main.type <> 1");
		buffer.append(" and detail.status in (1,2,3,5,12,13,15)");
		buffer.append(" and main.startTime is not null");
		
		//buffer.append(" and detail.sendAccountId = :orgAccountId");
		//parameterMap.put("orgAccountId", String.valueOf(conditionVo.getStatRangeId()));
		if("v3x_edoc_sign_count".equals(conditionVo.getStatType())){//只有本单位发文各单位/部门签收情况才会带发送方条件
			buffer.append(" and summary.orgAccountId = :orgAccountId");
			parameterMap.put("orgAccountId", conditionVo.getStatRangeId());
		}
		
		List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();
		if(Strings.isNotEmpty(statRangeList)) {
			buffer.append(" and (");
			for(int i=0; i<statRangeList.size(); i++) {
				if(i != 0) {
					buffer.append(" or");
				}
				buffer.append(" detail.recOrgId = :recOrgId" + i);
				parameterMap.put("recOrgId" + i, statRangeList.get(i));
			}
			buffer.append(")");
		}
		
		if(conditionVo.getStartDate() != null) {
			buffer.append(" and main.startTime >= :startDate");
			parameterMap.put("startDate", conditionVo.getStartDate());
		}
		if(conditionVo.getEndDate() != null) {
			buffer.append(" and main.startTime <= :endDate");
			parameterMap.put("endDate", conditionVo.getEndDate());
		}
		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap);
	
	}
	
	private static String selectStatDetailListSummary = "summary.id,summary.subject, summary.docMark,summary.serialNo,summary.state, "
			+ "summary.startUserId,summary.deadlineDatetime,summary.deadline,summary.hasArchive, summary.unitLevel, "
			+ "summary.issuer,summary.orgAccountId, summary.startTime,summary.archiveId, summary.templeteId, "
			+ "summary.signingDate,summary.completeTime,summary.registrationDate, summary.receiptDate, summary.govdocType";
	
	
	/**
	 * 工作统计穿透
	 * @param flipInfo
	 * @param conditionMap
	 * @return
	 * @throws Exception
	 */
    @SuppressWarnings("unchecked")
	public List<Object[]> statToListGovdoc(FlipInfo flipInfo, int listType, GovdocStatConditionVO conditionVo) throws Exception {
		//统计超期需要summaryIdList
		int overTime = GovdocStatListTypeEnum.getEnumByKey(listType).overTime();
		if(overTime==1 && Strings.isEmpty(conditionVo.getSummaryIdList())) {
			return null;
		}
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder buffer = new StringBuilder();
		buffer.append("select distinct " + selectStatDetailListSummary);	
		buffer.append(getGovdocListSQL(listType, conditionVo, parameterMap, false));
		buffer.append(" order by summary.startTime desc");

		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap, flipInfo);
    }
	
	/**
	 * 新公文工作统计-统计发文数、发文办理中、发文已办结、收文数、收文办理中、收文已办结
	 * @param listType
	 * @param conditionVo
	 * @return displayId, summaryId
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findGovdocStatResult(int listType, GovdocStatConditionVO conditionVo) throws Exception {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		
		StringBuilder buffer = new StringBuilder();
		buffer.append("select result.displayId as displayId, result.summaryId as summaryId from (");
		
		if("Department".equals(conditionVo.getStatRangeType())) {//统计人员
			buffer.append("select distinct summary.id as summaryId, OrgMember.id as displayId");
		} else {//统计单位、部门
			buffer.append("select distinct summary.id as summaryId, OrgUnit.id as displayId");
		}
		
		buffer.append(getGovdocListSQL(listType, conditionVo, parameterMap, true));
		
		buffer.append(") result group by result.displayId, result.summaryId");

		List<Object[]> scalarList = new ArrayList<Object[]>();
		scalarList.add(new Object[]{"displayId", Hibernate.LONG});
		scalarList.add(new Object[]{"summaryId", Hibernate.LONG});
		List<Object[]> list = (List<Object[]>)findBySQL(buffer.toString(), parameterMap, scalarList);
		return list;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object findBySQL(final String sql, final Map<String, Object> parameterMap, final List<Object[]> scalarList) {
		Pagination.setMaxResults(-1);
		Pagination.setFirstResult(-1);
		CTPHibernateDaoSupport dao = (CTPHibernateDaoSupport) AppContext.getThreadContext(GlobalNames.SPRING_HIBERNATE_DAO_SUPPORT);
    	if(dao==null){
    		dao =  (CTPHibernateDaoSupport) AppContext
                    .getBean("hibernateDaoSupport");
    	}
		return (Object) dao.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery query = session.createSQLQuery(sql);
				setParameter(parameterMap, query);
				if(Strings.isNotEmpty(scalarList)) {
					for(Object[] object:scalarList){
						query.addScalar(object[0].toString(), (Type)object[1]);
					}
				}
				List list = query.list();
				return list;
			}
		});
		
	}
	
	/**
	 * 新公文工作统计/统计穿透 获取SQL/HQL
	 * @param listType
	 * @param conditionVo
	 * @param parameterMap
	 * @param isSQL
	 * @return
	 */
	private String getGovdocListSQL(int listType, GovdocStatConditionVO conditionVo, Map<String, Object> parameterMap, boolean isSQL) {
		Date startDate = conditionVo.getStartDate();
		Date endDate = conditionVo.getEndDate();
		Long displayId = conditionVo.getDisplayId();
		String displayType = conditionVo.getDisplayType();
		int govdocType = GovdocStatListTypeEnum.getEnumByKey(listType).value();
		int state = GovdocStatListTypeEnum.getEnumByKey(listType).state();
		int overTime = GovdocStatListTypeEnum.getEnumByKey(listType).overTime();
		String fawenNodePolicy = conditionVo.getStatSetVo().getFawenNodePolicy();
		String shouwenNodePolicy = conditionVo.getStatSetVo().getShouwenNodePolicy();
		List<Long> statRangeList = conditionVo.getStatSetVo().getStatRangeList();

		StringBuilder buffer = new StringBuilder();
		buffer.append(" from "+GovdocStatUtil.getSQLColumn("Edoc_Summary", isSQL)+" summary");
		buffer.append(" , "+GovdocStatUtil.getSQLColumn("Ctp_Affair", isSQL)+" affair");
		buffer.append(" , "+GovdocStatUtil.getSQLColumn("Org_Member", isSQL)+" OrgMember");
		//发文无节点权限或拟文节点权限表示发文已发
		buffer.append(" , "+GovdocStatUtil.getSQLColumn("Org_Unit", isSQL)+" OrgUnit");
		buffer.append(" where summary.id = affair."+GovdocStatUtil.getSQLColumn("object_Id", isSQL));
		buffer.append(" and OrgUnit.id = OrgMember."+GovdocStatUtil.getSQLColumn("org_Department_Id", isSQL));
		
		//非待发
		buffer.append(" and summary."+GovdocStatUtil.getSQLColumn("case_Id", isSQL)+" is not null");
		buffer.append(" and summary.state in (0,1,3)");
		
		//流程结束
		if(state == GovdocFinishStateEnum.finished.key()) {
			buffer.append(" and summary."+GovdocStatUtil.getSQLColumn("complete_Time", isSQL)+" is not null");
		} else if(state==GovdocFinishStateEnum.pending.key()) {
			buffer.append(" and summary."+GovdocStatUtil.getSQLColumn("complete_Time", isSQL)+" is null");
		}
		
		//非超期穿透需要该过滤
		if(conditionVo.getDisplayId() == null || overTime != GovdocOverTimeEnum.yes.key()) {
			if(govdocType != -1) {
				buffer.append(" and (");
				buffer.append(		getAffairSQL(govdocType, parameterMap, fawenNodePolicy, shouwenNodePolicy, isSQL));
				buffer.append(" )");
			} else {
				buffer.append(" and (");
				//发文
				buffer.append(" 	(");
				buffer.append(			getAffairSQL(ApplicationSubCategoryEnum.edoc_fawen.key(), parameterMap, fawenNodePolicy, shouwenNodePolicy, isSQL));
				buffer.append(" 	)");
				//收文
				buffer.append(" 	or (");
				buffer.append(			getAffairSQL(ApplicationSubCategoryEnum.edoc_shouwen.key(), parameterMap, fawenNodePolicy, shouwenNodePolicy, isSQL));
				buffer.append( GovdocStatUtil.getSerialNoSql(conditionVo, isSQL));
				buffer.append(" 	)");
				buffer.append(" )");
			}
		}

		//统计穿透
		if(conditionVo.getDisplayId() != null) {
			//超期穿透
			if(overTime == GovdocOverTimeEnum.yes.key()) {
				buffer.append(" and affair.state in (2,3,4)");
				buffer.append(" and affair."+GovdocStatUtil.getSQLColumn("is_cover_time", "coverTime", isSQL)+" = :isCoverTime");
				if(isSQL) {
					parameterMap.put("isCoverTime", GovdocOverTimeEnum.yes.key());
				} else {
					parameterMap.put("isCoverTime", Boolean.TRUE);
				}
			}
			
			if(Strings.isNotEmpty(conditionVo.getSummaryIdList())) {
				buffer.append(" and affair." + GovdocStatUtil.getSQLColumn("object_Id", isSQL) + " in (:summaryIdList)");
				parameterMap.put("summaryIdList", conditionVo.getSummaryIdList());
			}
			
			if("Department".equals(displayType)) {
				if(Strings.isNotEmpty(statRangeList)) {
					buffer.append(" and (");
					for(int i=0; i<statRangeList.size(); i++) {
						if(i != 0) {
							buffer.append(" or");
						}
						buffer.append(" OrgUnit.id = :OrgUnit" + i);
						parameterMap.put("OrgUnit" + i, statRangeList.get(i));
					}
					buffer.append(")");
				}
			} else {
				buffer.append(" and OrgMember.id = :displayId");
				parameterMap.put("displayId", displayId);
			}	
		} else {
			if(Strings.isNotEmpty(statRangeList)) {
				buffer.append(" and (");
				for(int i=0; i<statRangeList.size(); i++) {
					if(i != 0) {
						buffer.append(" or");
					}
					buffer.append(" OrgUnit.id = :OrgUnit" + i);
					parameterMap.put("OrgUnit" + i, statRangeList.get(i));
				}
				buffer.append(")");
			}
		}
		
		buffer.append(GovdocStatUtil.getStatDocMarkSQL(govdocType, conditionVo, isSQL));

		if(startDate != null) {
			buffer.append(" and summary."+GovdocStatUtil.getSQLColumn("start_Time", isSQL)+" >= :startDate");
			parameterMap.put("startDate", startDate);
		}
		if(endDate != null) {
			buffer.append(" and summary."+GovdocStatUtil.getSQLColumn("start_Time", isSQL)+" <= :endDate");
			parameterMap.put("endDate", endDate);
		}
		
		return buffer.toString();
	}
	
	/**
	 * 获取公文超期的件数
	 * @param listType
	 * @param conditionVo
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findAffairOverList(int listType, GovdocStatConditionVO conditionVo) {
		List<Long> summaryIdList = conditionVo.getSummaryIdList();
		StringBuilder buffer = new StringBuilder();
		buffer.append("select affair.objectId, count(*) as countEdoc from CtpAffair affair");
		buffer.append(" where affair.state in (2,3,4)");
		buffer.append(" and affair.coverTime = :isCoverTime");
		buffer.append(" and affair.objectId in (:summaryIdList)");
		buffer.append(" group by affair.objectId");
		
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		parameterMap.put("isCoverTime", Boolean.TRUE);
		parameterMap.put("summaryIdList", summaryIdList);
		
		return (List<Object[]>)DBAgent.find(buffer.toString(), parameterMap);
	}

	/**
	 * 根据summaryId查询正文字数
	 * @param summaryIdList
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findFontSizeBySummaryId(List<Long> summaryIdList) throws Exception {
		if(Strings.isEmpty(summaryIdList)) {
			return null;
		}
		
        Map<String, Object> parameterMap = new HashMap<String, Object>();
        parameterMap.put("summaryIdList", summaryIdList);
		String sql = "select id from EdocSummary where id in (:summaryIdList)";
		
		return (List<Object[]>)DBAgent.find(sql, parameterMap);
	}
	
	/**
	 * 
	 * @param parameterMap
	 * @param docMark
	 * @param docMarkDefId
	 * @param serialNo
	 * @param serialNoDefId
	 * @param isHql
	 * @return
	 */
	public static String getStatDocMarkSQL(int govdocType, GovdocStatConditionVO conditionVo, boolean isSQL) {
		StringBuilder buffer = new StringBuilder();
		//公文文号过滤发文
		if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			if(conditionVo.getDocMarkDefId()!=null && !"-1".equals(conditionVo.getDocMarkDefId()) && conditionVo.getDocMark() != null) {
				EdocMarkDefinition markDef = conditionVo.getDocMarkDef();
				if(markDef != null) {
					String expression = markDef.getExpression();
					expression = expression.replace("$WORD", "");
					expression = expression.replace("$YEAR", "%");
					expression = expression.replace("$NO", "%");
					expression = markDef.getWordNo() + expression;
					buffer.append(" and summary." + getSQLColumn("doc_Mark", isSQL) + " like '" + expression + "'");
				}
			}
		}
		//内部文号过滤收文	
		if(govdocType == ApplicationSubCategoryEnum.edoc_shouwen.key()) {
			if(conditionVo.getSerialNoDefId()!=null && !"-1".equals(conditionVo.getSerialNoDefId()) && conditionVo.getSerialNo() != null) {
				EdocMarkDefinition markDef = conditionVo.getSerialNoMarkDef();
				if(markDef != null) {
					String expression = markDef.getExpression();
					expression = expression.replace("$WORD", "");
					expression = expression.replace("$YEAR", "%");
					expression = expression.replace("$NO", "%");
					expression = markDef.getWordNo() + expression;
					buffer.append(" and summary." + getSQLColumn("serial_No", isSQL) + " like '" + expression + "'");
				}
			}
		}
		return buffer.toString();
	}
	
	public static String getSQLColumn(String column, boolean isSQL) {
		if(isSQL) {
			return column;
		} else {
			return column.replace("_", ""); 
		}
	}
	
	
	public static String getSQLColumn(String column, String field, boolean isSQL) {
		if(isSQL) {
			return column;
		} else {
			return field;
		}
	}
	
	
	/**
	 * 统计公文Affair的状态过滤
	 * @param govdocType
	 * @param parameterMap
	 * @param fawenNodePolicy
	 * @param shouwenNodePolicy
	 * @param isSQL
	 * @return
	 */
	private String getAffairSQL(int govdocType, Map<String, Object> parameterMap, String fawenNodePolicy, String shouwenNodePolicy, boolean isSQL) {
		boolean isFawenNiwen = false;
		boolean isShouwenDengj = false;
		if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			if(Strings.isBlank(fawenNodePolicy) || "niwen".equals(fawenNodePolicy)) {
				isFawenNiwen = true;
			}
		} else {
			if("dengji".equals(shouwenNodePolicy)) {
				isShouwenDengj = true;
			}
		}
		
		StringBuilder buffer = new StringBuilder();
		
		buffer.append(" (affair."+GovdocStatUtil.getSQLColumn("app", isSQL)+" = :app");
		parameterMap.put("app", ApplicationCategoryEnum.edoc.getKey());
		if(govdocType == ApplicationSubCategoryEnum.edoc_fawen.key()) {
			buffer.append(" and (affair."+GovdocStatUtil.getSQLColumn("sub_App", isSQL)+" = :sub_App1"); 
			parameterMap.put("sub_App1", govdocType);
			buffer.append(" or affair."+GovdocStatUtil.getSQLColumn("sub_App", isSQL)+" = :sub_App2))");
			//发文-本部门已发的数据
			parameterMap.put("sub_App2", ApplicationCategoryEnum.edocSend.key());
			buffer.append(" and OrgMember.id = affair."+GovdocStatUtil.getSQLColumn("sender_Id", isSQL));
			if(!isFawenNiwen) {
				buffer.append(" and affair."+GovdocStatUtil.getSQLColumn("node_Policy", isSQL)+" = :fawenNodePolicy");
				parameterMap.put("fawenNodePolicy", fawenNodePolicy);
				
				buffer.append(" and affair.state = :fawenAffairState");
				parameterMap.put("fawenAffairState", StateEnum.col_done.key());	
			} else {
				buffer.append(" and affair.state in (2,3,4)");
			}
		} else {	
			buffer.append(" and (affair."+GovdocStatUtil.getSQLColumn("sub_App", isSQL)+" = :sub_App3"); 
			parameterMap.put("sub_App3", govdocType);
			buffer.append(" or affair."+GovdocStatUtil.getSQLColumn("sub_App", isSQL)+" = :sub_App4))");
			parameterMap.put("sub_App4", ApplicationCategoryEnum.edocRec.key());
			buffer.append(" and OrgMember.id = affair."+GovdocStatUtil.getSQLColumn("member_Id", isSQL));
			if(!isShouwenDengj) {
				buffer.append(" and affair."+GovdocStatUtil.getSQLColumn("node_Policy", isSQL)+" = :shouwenNodePolicy");
				parameterMap.put("shouwenNodePolicy", shouwenNodePolicy);
				
				buffer.append(" and affair.state = :shouwenAffairState");
				parameterMap.put("shouwenAffairState", StateEnum.col_done.key());
			} else {
				buffer.append(" and affair.state = :shouwenAffairState");
				parameterMap.put("shouwenAffairState", StateEnum.col_sent.key());
			}
		}
		return buffer.toString();
	}
	
	
	/**
	 * 公文统计-经办/已发
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findEdocStatResultBySql(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {	    
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
		int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		//获取统计部分sql
		String sqlDisplayId = getDisplayByDisplayType(displayType, displayTimeType);
		String sqlDisplayColumn = getDisplayColumnByResultType(resultType);
		String sqlCountDisplayColumn = getCountDisplayColumnByResultType(resultType);
		//拼装统计sql
		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + sqlDisplayId + ", " + sqlDisplayColumn+", " + sqlCountDisplayColumn);
		buffer.append(" from (");
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key()
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {//待办/已办/待阅/已阅
			buffer.append(getEdocStatResultSQL_subQuery(params, parameterMap));
		} else {
			buffer.append(getEdocStatResultSQL(params, parameterMap));	
		}
		buffer.append(")  result group by "+ sqlDisplayId +", " + sqlDisplayColumn);
		//返回查询结果
		List<Object[]> list = (List<Object[]>)findBySQL(buffer.toString(), parameterMap);
		return list;
	}
	
	/**
	 * 公文统计-经办
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findEdocStatResultBySql_DoAndRead(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {	    
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder buffer = new StringBuilder();		
		User user = AppContext.getCurrentUser();		
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
        //获取统计部分sql
        String sqlGroupByDisplayType = getTableColumnGroupByDisplayType(displayType, displayTimeType, resultType);
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		GovdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		Integer state = GovdocStatHelper.getResultAffairState(resultType);
		Integer subApp = GovdocStatHelper.getResultAffairSubApp(resultType);
        //拼装统计sql		
		buffer.append(" select " + sqlGroupByDisplayType +  ", count(distinct stat.edoc_id) ");
        buffer.append(" from edoc_stat stat, ctp_affair affair, org_member OrgMember");
		buffer.append(" where affair.member_id=OrgMember.id and stat.edoc_id=affair.object_id");
		//统计公文类型
		buffer.append(" and stat.edoc_type = :edocType");
		parameterMap.put("edocType", edocType);
		//公文统计人员公文所属单位
        buffer.append(" and OrgMember.org_account_id = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        if(!isCurrentAccountExchange && isCurrentDeptExchange) {
	        buffer.append(" and OrgMember.org_department_id in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	buffer.append(" and OrgMember.org_department_id in (:deptIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and OrgMember.id in (:memberIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and (OrgMember.org_department_id in (:deptIdList) or OrgMember.id in (:memberIdList))");
        }
        //处理状态
        buffer.append(" and affair.state = :state");
        parameterMap.put("state", state);
        
        //公文类型
        buffer.append(" and affair.app = :app and affair.sub_app in (:subApp)");
        List<Integer> subAppList = new ArrayList<Integer>();
        subAppList.add(GovdocStatHelper.getEdocSubApp(edocType));
        subAppList.add(GovdocStatHelper.getOldEdocSubApp(edocType));
        parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
        parameterMap.put("subApp", subAppList);
        
        //公文子类型
        boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
        if(subApp != null) {
	        if(showBanwenYuewen) {
	        	buffer.append(" and stat.process_type = :recHandle");
                if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {//办件
                	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecHandle.key());
                } else {//阅件
                	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecRead.key());
                }
            }
        }
        
        buffer.append(getStatByConditionSql(params, parameterMap));
        buffer.append(" group by " + sqlGroupByDisplayType +  ", stat.edoc_id");
        //返回查询结果
		List<Object[]> list = (List<Object[]>)findBySQL(buffer.toString(), parameterMap);
		return list;
	}
	
	/**
	 * 公文统计-经办
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findEdocStatResultBySql_Sent(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {	    
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder buffer = new StringBuilder();		
		User user = AppContext.getCurrentUser();		
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
        //获取统计部分sql
        String sqlGroupByDisplayType = getTableColumnGroupByDisplayType(displayType, displayTimeType, resultType);
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		GovdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
        //拼装统计sql		
		buffer.append(" select " + sqlGroupByDisplayType +  ", count(distinct stat.edoc_id) ");
        buffer.append(" from edoc_stat stat");
		//统计公文类型
		buffer.append(" where stat.edoc_type = :edocType");
		parameterMap.put("edocType", edocType);
        //已发-发起单位为当前单位
        buffer.append(" and stat.account_id = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        //已发-部门收发员则发起部门为当前部门
        if(!isCurrentAccountExchange && isCurrentDeptExchange) {
	        buffer.append(" and stat.org_department_id in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	buffer.append(" and stat.org_department_id in (:deptIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and stat.create_user_id in (:memberIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and (stat.org_department_id in (:deptIdList) or  stat.create_user_id in (:memberIdList))");
        }
        buffer.append(getStatByConditionSql(params, parameterMap));
        buffer.append(" group by " + sqlGroupByDisplayType +  ", stat.edoc_id");
        //返回查询结果
		List<Object[]> list = (List<Object[]>)findBySQL(buffer.toString(), parameterMap);
		return list;
	}
	
	/**
	 * 公文统计-经办Sql
	 * @param params
	 * @param parameterMap
	 * @return
	 * @throws BusinessException
	 */
	private String getEdocStatResultSQL(Map<String, String> params, Map<String, Object> parameterMap) throws BusinessException {
		User user = AppContext.getCurrentUser();		
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		GovdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
        //获取统计部分sql
        String sqlColumnAsDisplayId = getTableColumnAsDisplayIdByDisplayType(displayType, displayTimeType, resultType);
        String sqlColumnAs = getTableColumnAsByResultType(resultType);
        //拼装统计sql
		StringBuilder buffer = new StringBuilder();
		buffer.append(" select distinct " + sqlColumnAsDisplayId + "," + sqlColumnAs);
		if (edocType == 1) {
			buffer.append(" from edoc_stat stat, ctp_affair affair, org_member OrgMember, edoc_summary summary");
			buffer.append(" where affair.member_id=OrgMember.id and stat.edoc_id=affair.object_id and summary.id=stat.edoc_id ");
		}else{
			buffer.append(" from edoc_stat stat, ctp_affair affair, org_member OrgMember ");
			buffer.append(" where affair.member_id=OrgMember.id and stat.edoc_id=affair.object_id ");
		}
		//统计公文类型
		buffer.append(" and stat.edoc_type = :edocType");
		parameterMap.put("edocType", edocType);
        //处理状态
        List<Integer> stateList = GovdocStatHelper.getResultAffairStateList(resultType);
        List<Integer> subStateList = GovdocStatHelper.getResultAffairSubStateList(resultType);
        Integer subApp = GovdocStatHelper.getResultAffairSubApp(resultType);
        if(Strings.isNotEmpty(stateList)) {
        	buffer.append(" and affair.state in (:stateList)");
        	parameterMap.put("stateList", stateList);
        }
        if(Strings.isNotEmpty(subStateList)) {
        	buffer.append(" and affair.sub_state in (:subStateList)");
        	parameterMap.put("subStateList", subStateList);
        }
        
        buffer.append(" and affair.app = :app and affair.sub_app in (:subApp)");
        //TODO summary.TRANSFER_STATUS not in(-1,7,3)
        List<Integer> subAppList = new ArrayList<Integer>();
        subAppList.add(GovdocStatHelper.getEdocSubApp(edocType));
        subAppList.add(GovdocStatHelper.getOldEdocSubApp(edocType));
        parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
        parameterMap.put("subApp", subAppList);
        
        //公文子类型
        boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
        if(subApp != null) {
	        if(showBanwenYuewen) {
	        	buffer.append(" and stat.process_type = :recHandle");
                if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {//办件
                	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecHandle.key());
                } else {//阅件
                	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecRead.key());
                }
            }
        }
        buffer.append(" and OrgMember.org_account_id = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        if(!isCurrentAccountExchange && isCurrentDeptExchange) {
	        buffer.append(" and OrgMember.org_department_id in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	buffer.append(" and OrgMember.org_department_id in (:deptIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and OrgMember.id in (:memberIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and (OrgMember.org_department_id in (:deptIdList) or OrgMember.id in (:memberIdList))");
        }
        buffer.append(getStatByConditionSql(params, parameterMap));
        //buffer.append(" group by " + sqlGroupByDisplayType + ", stat.edoc_id, " + sqlGroupByResultType);
        return buffer.toString();
	}
	
	/**
	 * 公文统计-已发Sql
	 * @param params
	 * @param parameterMap
	 * @return
	 * @throws BusinessException
	 */
	private String getEdocStatResultSQL_subQuery(Map<String, String> params, Map<String, Object> parameterMap) throws BusinessException {
		StringBuilder buffer = new StringBuilder();		
		User user = AppContext.getCurrentUser();		
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
        //获取统计部分sql
        String sqlColumnAsDisplayId = getTableColumnAsDisplayIdByDisplayType(displayType, displayTimeType, resultType);
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		GovdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Integer> stateList = GovdocStatHelper.getResultAffairStateList(resultType);
        //拼装统计sql		
		buffer.append(" select distinct " + sqlColumnAsDisplayId +  ", stat.edoc_id as edocId, affair.state as affairState");
        buffer.append(" from edoc_stat stat, ctp_affair affair, org_member OrgMember");
		buffer.append(" where affair.member_id=OrgMember.id and stat.edoc_id=affair.object_id");
		//统计公文类型
		buffer.append(" and stat.edoc_type = :edocType");
		parameterMap.put("edocType", edocType);
		//公文统计人员公文所属单位
        buffer.append(" and OrgMember.org_account_id = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        if(!isCurrentAccountExchange && isCurrentDeptExchange) {
	        buffer.append(" and OrgMember.org_department_id in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	buffer.append(" and OrgMember.org_department_id in (:deptIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and OrgMember.id in (:memberIdList)");
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	buffer.append(" and (OrgMember.org_department_id in (:deptIdList) or OrgMember.id in (:memberIdList))");
        }
        //处理状态
        if(Strings.isNotEmpty(stateList)) {
        	buffer.append(" and (");
        	for(int i=0; i<stateList.size(); i++) {
        		if(i > 0) {
        			buffer.append(" or");
        		}
        		buffer.append(" affair.state = :state"+i);
        		parameterMap.put("state"+i, stateList.get(i));
        	}
        	buffer.append(")");
        }
        
        //公文类型
        buffer.append(" and (affair.app = :app and affair.sub_app in (:subApp))");
        List<Integer> subAppList = new ArrayList<Integer>();
        subAppList.add(GovdocStatHelper.getEdocSubApp(edocType));
        subAppList.add(GovdocStatHelper.getOldEdocSubApp(edocType));
        parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
        parameterMap.put("subApp", subAppList);
        
        boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
        if(showBanwenYuewen) {
        	//待办/已办//待阅/已阅
        	if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key()
        			|| resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {
        		buffer.append(" and stat.process_type = :recHandle");
        		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key()) {//待办/已办
        			parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecHandle.key());
        		} else {//待阅/已阅
        			parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecRead.key());
        		}
        	}
        }
        
        buffer.append(getStatByConditionSql(params, parameterMap));
		return buffer.toString();
	}
	
	/**
	 * 
	 * @param displayType
	 * @param displayTimeType
	 * @return
	 */
	private String getDisplayByDisplayType(int displayType, int displayTimeType) {
		String sqlDisplay = "";
		if(displayType == 1 || displayType == 2) {
        	sqlDisplay = "result.displayId";
        } else {
        	if(displayTimeType == 1) {//年
        		sqlDisplay = "result.statYear";
			} else if(displayTimeType == 2) {//季
				sqlDisplay = "result.statYear, result.statQuarter";
			} else if(displayTimeType == 3) {//月
				sqlDisplay = "result.statYear, result.statMonth";
			} else {//日
				sqlDisplay = "result.statYear, result.statMonth, result.statDay";
			}
        }
		return sqlDisplay;
	}
	
	/**
	 * 
	 * @param resultType
	 * @return
	 */
	private String getDisplayColumnByResultType(int resultType) {
		String sqlDisplayColumn = "";
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAll.key()) {//总经办
			sqlDisplayColumn = "result.flowState";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {//总阅件
			sqlDisplayColumn = "result.affairSubApp";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {//承办数
			sqlDisplayColumn = "result.affairNodePolicy";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
			sqlDisplayColumn = "result.affairState";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key() ||
				resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key() ||
				resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key() ||
				resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {
			sqlDisplayColumn = "result.affairState";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key() ||
				resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {
			sqlDisplayColumn = "result.affairState";
		} else {
			sqlDisplayColumn = "result.affairState, result.affairSubApp";
		}
		return sqlDisplayColumn;
	}
	
	private String getCountDisplayColumnByResultType(int resultType) {
		String sqlDisplayColumn = "";
		if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAll.key()) {//总经办
			sqlDisplayColumn = "count(result.flowState)";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {//总阅件
			sqlDisplayColumn = "count(result.affairSubApp)";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {//承办数
			sqlDisplayColumn = "count(result.affairNodePolicy)";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()
				|| resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {//已发
			sqlDisplayColumn = "count(result.affairState)";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.pendingAndDone.key() ||
				resultType == EdocStatEnum.EdocStatResultTypeEnum.readingAndReaded.key()) {
			sqlDisplayColumn = "count(result.edocId)";
		} else {
			sqlDisplayColumn = "count(result.affairState), count(result.affairSubApp)";
		}
		return sqlDisplayColumn;
	}
	
	/**
	 * 
	 * @param displayType
	 * @param displayTimeType
	 * @return
	 */
	private String getTableColumnGroupByDisplayType(int displayType, int displayTimeType, int resultType) {
		String sqlOrdreByDisplay = "";
        if(displayType == EdocStatDisplayTypeEnum.department.key()) {
        	if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
        		sqlOrdreByDisplay = "stat.org_department_id";
        	} else {
        		sqlOrdreByDisplay = "OrgMember.org_department_id";
        	}
        } else if(displayType == EdocStatDisplayTypeEnum.member.key()) {
        	if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
        		sqlOrdreByDisplay = "stat.create_user_id";
        	} else {
        		sqlOrdreByDisplay = "OrgMember.id";
        	}
        } else {
        	if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年
        		sqlOrdreByDisplay = "stat.year";	
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季
				sqlOrdreByDisplay = "stat.year, stat.stat_quarter";
			} else if(displayTimeType== EdocStatDisplayTimeTypeEnum.month.key()) {//月
				sqlOrdreByDisplay = "stat.year, stat.month";
			} else {//日
				sqlOrdreByDisplay = "stat.year, stat.month, stat.stat_day";
			}
        }
        return sqlOrdreByDisplay;
	}
	
	/**
	 * 
	 * @param resultType
	 * @return
	 */
	@SuppressWarnings("unused")
	private String getTableColumnGroupByResultType(int resultType) {
		String sqlGroupBy = "";
		 if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAll.key()) {//总经办
			 sqlGroupBy = "stat.flow_state";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {//总阅件
			sqlGroupBy = "affair.sub_app";
		} else if(resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {//总阅件
			sqlGroupBy = "affair.node_policy";
        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {// 已发
            sqlGroupBy = "affair.state";
        } else {
            sqlGroupBy = "affair.state, affair.sub_app";
        }
		return sqlGroupBy;
	}
	
	/**
	 * 
	 * @param displayType
	 * @param displayTimeType
	 * @return
	 */
	private String getTableColumnAsDisplayIdByDisplayType(int displayType, int displayTimeType, int resultType) {
		String sqlDisplay = "";
        if(displayType == EdocStatDisplayTypeEnum.department.key()) {
        	if(resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()) {//已发
        		sqlDisplay = "stat.org_department_id as displayId";
        	} else {
        		sqlDisplay = "OrgMember.org_department_id as displayId";
        	}
        } else if(displayType == EdocStatDisplayTypeEnum.member.key()) {
        	sqlDisplay = "OrgMember.id as displayId";
        } else {
        	if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年
        		sqlDisplay = "stat.year as statYear";	
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季
				sqlDisplay = "stat.year as statYear, stat.stat_quarter as statQuarter";
			} else if(displayTimeType  == EdocStatDisplayTimeTypeEnum.month.key()) {//月
				sqlDisplay = "stat.year as statYear, stat.month as statMonth";
			} else {//日
				sqlDisplay = "stat.year as statYear, stat.month as statMonth, stat.stat_day as statDay";
			}
        }
        return sqlDisplay;
	}
	
	/**
	 * 结果类型
	 * @param resultType
	 * @return
	 */
	private String getTableColumnAsByResultType(int resultType) {
		String sqlColumn = "";
        if(resultType == EdocStatEnum.EdocStatResultTypeEnum.doAll.key()) {//总经办
        	sqlColumn = "stat.edoc_id as edocId, stat.flow_state as flowState";
        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.readAll.key()) {// 总阅件
            sqlColumn = "stat.edoc_id as edocId, affair.sub_app as affairSubApp";
        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.undertaker.key()) {// 总阅件
            sqlColumn = "stat.edoc_id as edocId, affair.node_policy as affairNodePolicy";
        } else if (resultType == EdocStatEnum.EdocStatResultTypeEnum.sent.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.pending.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.done.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.reading.key()
                || resultType == EdocStatEnum.EdocStatResultTypeEnum.readed.key()) {// 已发
            sqlColumn = "stat.edoc_id as edocId, affair.state as affairState";
        } else {
            sqlColumn = "stat.edoc_id as edocId, affair.state as affairState, affair.sub_app as affairSubApp";
        }
        return sqlColumn;
	}	

	/**
	 * 统计条件筛选Sql
	 * @param params
	 * @param parameterMap
	 * @return
	 */
	private String getStatByConditionSql(Map<String, String> params, Map<String, Object> parameterMap) {
		String unitLevelId = (String) params.get("unitLevelId");
		String sendTypeId = (String) params.get("sendTypeId");
		Date startTime = params.get("startRangeTime")==null ? null : Datetimes.parseDatetime(params.get("startRangeTime")+" 00:00:00");
		Date endTime = params.get("endRangeTime")==null ? null : Datetimes.parseDatetime(params.get("endRangeTime")+" 23:59:59");
		String operationTypeIdsStr = (String) params.get("operationTypeIds");
		String operationType = (String) params.get("operationType");
		String[] operationTypeIds = null;
    	if(operationTypeIdsStr!=null&&!"".equals(operationTypeIdsStr.trim())){
    		operationTypeIds = operationTypeIdsStr.split(",");
    	}
    	StringBuilder tempSQL = new StringBuilder();
		if(Strings.isNotBlank(unitLevelId)) {
        	List<String> unitLevelIdList = new ArrayList<String>();
        	for(String unitLevel : unitLevelId.split(",")) {
        		unitLevelIdList.add(unitLevel);
        	}
        	tempSQL.append(" and stat.unit_level in (:unitLevelList)");
        	parameterMap.put("unitLevelList", unitLevelIdList);
        }
        if(Strings.isNotBlank(sendTypeId)) {
        	List<String> sendTypeList = new ArrayList<String>();
        	for(String sendType : sendTypeId.split(",")) {
        		sendTypeList.add(sendType);
        	}
        	tempSQL.append(" and stat.send_type in (:sendTypeList)");
        	parameterMap.put("sendTypeList", sendTypeList);
        }
        if(startTime!=null && endTime!=null) {
        	tempSQL.append(" and (stat.create_date between :startTime and :endTime)");
        	parameterMap.put("startTime", startTime);
            parameterMap.put("endTime", endTime);
        }
        StringBuilder templateHSQL = new StringBuilder();
        if(Strings.isNotBlank(operationType)) {
        	boolean sysTemplateNotNull = operationTypeIds!=null && !"".equals(operationTypeIds[0]);
        	if(operationType.contains("template")) {//系统模板
		        if(sysTemplateNotNull) {//模板过滤不为空
		        	templateHSQL.append(" and (");
		        	templateHSQL.append(" stat.templete_id in (");
		    		for(int i=0; i<operationTypeIds.length; i++){
		        		Long templeteId = Long.parseLong(operationTypeIds[i]);
		        		templateHSQL.append(templeteId);
		        		if(i != operationTypeIds.length-1) {
		        			templateHSQL.append(",");
		        		}
		    		}
		    		templateHSQL.append(")");
		    		if(operationType.contains("self")) {
		    			templateHSQL.append(" or stat.templete_id is null");
		    		}
		    		templateHSQL.append(")");
		        } else {
		        	if(!operationType.contains("self")) { 
		        		templateHSQL.append(" and stat.templete_id is not null");
		        	}
		        }
        	} 
        	else if(operationType.contains("self")) {//自由流程  	
        		templateHSQL.append(" and stat.templete_id is null");
        	}
        
        } else {
        	templateHSQL.append(" and stat.templete_id = :templeteId");//因为1=2索引会失效，这里使用一个非正常值
    		parameterMap.put("templeteId", -1);
    	}
    	tempSQL.append(templateHSQL);
		 return tempSQL.toString();
	}
	
	/**
	 * 公文统计-穿透列表
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findStatEdocList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		int edocType = Integer.parseInt(params.get("edocType"));
		int listType = Integer.parseInt(params.get("listType"));
		Map<String, Object> parameterMap = new HashMap<String, Object>();
    	StringBuilder buffer = new StringBuilder();
    	if(listType == EdocStatListTypeEnum.sent.key()) {//已发
    		buffer.append("select distinct "+selectStatListSummary + " from EdocSummary summary, CtpAffair affair");
    		buffer.append(" where summary.id=affair.objectId");
    		
    		buffer.append(getStatSentEdocListHQL(params, parameterMap));
    		
    		//已发或者指定回退-直接回退给我
    		buffer.append(" and (affair.state = :aState or (affair.state=:aState1 and affair.subState = :aSubState))");
    		parameterMap.put("aState", StateEnum.col_sent.getKey());
    		parameterMap.put("aState1", StateEnum.col_waitSend.getKey());
    		parameterMap.put("aSubState", SubStateEnum.col_pending_specialBacked.getKey());
    		
    		buffer.append(" and affair.app = :app and affair.subApp in (:subApp)");
            List<Integer> subAppList = new ArrayList<Integer>();
            subAppList.add(GovdocStatHelper.getEdocSubApp(edocType));
            subAppList.add(GovdocStatHelper.getOldEdocSubApp(edocType));
            parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
            parameterMap.put("subApp", subAppList);
        } else {//办理
        	buffer.append("select distinct "+selectStatListSummary + " from EdocSummary summary, CtpAffair affair, OrgMember OrgMember");
            buffer.append(" where summary.id=affair.objectId and affair.memberId=OrgMember.id");

            buffer.append(getStatDoneEdocListHQL(params, parameterMap));
            
            List<Integer> stateList = GovdocStatHelper.gerAffairStateList(listType);
            List<Integer> subStateList = GovdocStatHelper.gerAffairSubStateList(listType);
            Integer subApp = GovdocStatHelper.gerAffairSubApp(listType);
            
            if(Strings.isNotEmpty(stateList)) {
            	buffer.append(" and affair.state in (:stateList)");
            	parameterMap.put("stateList", stateList);
            }
            if(Strings.isNotEmpty(subStateList)) {
            	buffer.append(" and affair.subState in (:subStateList)");
            	parameterMap.put("subStateList", subStateList);
            }
            
            buffer.append(" and affair.app = :app and affair.subApp in (:subApp)");
            List<Integer> subAppList = new ArrayList<Integer>();
            subAppList.add(GovdocStatHelper.getEdocSubApp(edocType));
            subAppList.add(GovdocStatHelper.getOldEdocSubApp(edocType));
            parameterMap.put("app", ApplicationCategoryEnum.edoc.key());
            parameterMap.put("subApp", subAppList);
            
            if(subApp != null) {
//            	boolean showBanwenYuewen = GovdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
//            	if(showBanwenYuewen) {
//    	        	buffer.append(" and summary.processType = :recHandle");
//                    if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {//办件
//                    	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecHandle.key());
//                    } else {//阅件
//                    	parameterMap.put("recHandle", GovdocProcessTypeEnum.govdocRecRead.key());
//                    }
//                }
            }
        }
        List<Integer> summaryStateList = GovdocStatHelper.getSummaryStateList(listType);
        if(Strings.isNotEmpty(summaryStateList)) {
        	buffer.append(" and summary.state in (:state)");
        	parameterMap.put("state", summaryStateList);
        }
        buffer.append(getSummaryByConditionHql(params, parameterMap));
        if(Strings.isNotBlank(params.get("condition"))) {
        	if(Strings.isNotBlank(params.get("subject"))) {
        		 buffer.append(" and summary.subject like :subject");
                 parameterMap.put("subject", "%"+SQLWildcardUtil.escape(params.get("subject"))+"%");
        	}
        	if(Strings.isNotBlank(params.get("docMark"))) {
       		 	buffer.append(" and summary.docMark like :docMark");
                parameterMap.put("docMark", "%"+SQLWildcardUtil.escape(params.get("docMark"))+"%");
        	}
        	if(Strings.isNotBlank(params.get("serialNo"))) {
       		 	buffer.append(" and summary.serialNo like :serialNo");
                parameterMap.put("serialNo", "%"+SQLWildcardUtil.escape(params.get("serialNo"))+"%");
        	}
        	if(Strings.isNotBlank(params.get("sendUnit"))) {
       		 	buffer.append(" and summary.sendUnit like :sendUnit");
                parameterMap.put("sendUnit", "%"+SQLWildcardUtil.escape(params.get("sendUnit"))+"%");
        	}
        	if(Strings.isNotBlank(params.get("coverTime"))) {
       		 	Boolean isConvertTime = Boolean.FALSE;
       		 	if("1".equals(params.get("coverTime"))) {
       		 	    buffer.append(" and summary.coverTime = :coverTime");
       		 		isConvertTime = Boolean.TRUE; 	
       		 	}else {
       		 	    buffer.append(" and (summary.coverTime = :coverTime or summary.coverTime is null)");
                }
                parameterMap.put("coverTime", isConvertTime);
        	}
        	if(Strings.isNotBlank(params.get("nodePolicy"))) {//已办穿透才会有
        		buffer.append(" and affair.nodePolicy = :nodePolicy");
                parameterMap.put("nodePolicy", SQLWildcardUtil.escape(params.get("nodePolicy")));
        	}
        }
        buffer.append(" order by summary.startTime desc ");
        if(Strings.isNotBlank(params.get("isPager"))) {
        	return DBAgent.find(buffer.toString(), parameterMap);
        } else {
        	return DBAgent.find(buffer.toString(), parameterMap, flipInfo);
        }
	}
	
	/**
	 * 获取公文统计经办的穿透
	 * @param params
	 * @param parameterMap
	 * @return
	 * @throws BusinessException
	 */
	private String getStatDoneEdocListHQL(Map<String, String> params, Map<String, Object> parameterMap) throws BusinessException {
		User user = AppContext.getCurrentUser();
		String displayId = params.get("displayId");
		int displayType = Strings.isBlank(params.get("displayType")) ? -1 : Integer.parseInt(params.get("displayType"));
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> selectDeptIdList = GovdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = GovdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");
		boolean isAccountExchange = GovdocRoleHelper.isAccountExchange();
		StringBuilder buffer = new StringBuilder();
		/** 统计条件范围过滤 */
    	buffer.append(getStatRangeHql(deptIdList, memberIdList, parameterMap));
		/** 经办列表单独存在：单位经办 */
		buffer.append(" and OrgMember.orgAccountId in (:currentAccountId)");
		parameterMap.put("currentAccountId", user.getLoginAccount());
		/** 如选择了部门，则为选中部门人员接收 */
		if(Strings.isNotEmpty(selectDeptIdList)) {
			buffer.append(" and OrgMember.orgDepartmentId in (:selectDeptIdList)");
			parameterMap.put("selectDeptIdList", selectDeptIdList);
		}
		/** 部门收发员，数据为当前部门经办 */
    	if(!isAccountExchange && Strings.isNotEmpty(userSelfAndSubDeptIdList)) {
    		buffer.append(" and OrgMember.orgDepartmentId in (:userSelfAndSubDeptIdList)");
        	parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
		}
		if(displayType == 1) {//显示部门
        	buffer.append(" and OrgMember.orgDepartmentId=:displayId");
    		parameterMap.put("displayId", Long.parseLong(displayId));
    	} else if(displayType == 2) {//显示人员
	    	buffer.append(" and OrgMember.id=:displayId");
	     	parameterMap.put("displayId", Long.parseLong(displayId));
    	} else {//显示时间
    		int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? -1 : Integer.parseInt(params.get("displayTimeType"));
    		Date[] startTimes = GovdocStatHelper.getStartAndEndTime(displayTimeType, displayId);
        	buffer.append(" and (summary.startTime between :startTimeC and :startTimeE)");
        	parameterMap.put("startTimeC", startTimes[0]);
        	parameterMap.put("startTimeE", startTimes[1]);
    	}
     	return buffer.toString();
	}
	
	/**
	 * 获取公文统计已发的穿透脚本
	 * @param params
	 * @param parameterMap
	 * @return
	 * @throws BusinessException
	 */
	private String getStatSentEdocListHQL(Map<String, String> params, Map<String, Object> parameterMap) throws BusinessException {
		User user = AppContext.getCurrentUser();
		String displayId = params.get("displayId");
		int displayType = Strings.isBlank(params.get("displayType")) ? -1 : Integer.parseInt(params.get("displayType"));
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> selectDeptIdList = GovdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = GovdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");
		boolean isAccountExchange = GovdocRoleHelper.isAccountExchange();
		StringBuilder buffer = new StringBuilder();
		/** 统计条件范围过滤 */
		buffer.append(getSentStatRangeHql(deptIdList, memberIdList, parameterMap));
		/** 已发列表单独存在：本单位发出 */
		buffer.append(" and summary.orgAccountId = :currentAccountId");
    	parameterMap.put("currentAccountId", user.getLoginAccount());
    	/** 如选择了部门，则为选中部门发出 */
    	if(Strings.isNotEmpty(selectDeptIdList)) {
    		buffer.append(" and summary.orgDepartmentId in (:selectDeptIdList)");
	    	parameterMap.put("selectDeptIdList", selectDeptIdList);
    	}
    	/** 只是部门收发员，本部门(包括子部门)发出 */
    	if(!isAccountExchange && Strings.isNotEmpty(userSelfAndSubDeptIdList)) {
    		buffer.append(" and summary.orgDepartmentId in (:userSelfAndSubDeptIdList)");
         	parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);//包括子部门
    	}
		if(displayType == 1) {//显示部门
        	/** 过滤穿透的统计范围 */
        	buffer.append(" and summary.orgDepartmentId = :displayId");
        	parameterMap.put("displayId", Long.parseLong(displayId));	
    	} else if(displayType == 2) {//显示人员
    		buffer.append(" and summary.startUserId = :displayId");
			parameterMap.put("displayId", Long.parseLong(displayId));
    	} else if(displayType == 3) {//时间
    		int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? -1 : Integer.parseInt(params.get("displayTimeType"));
    		Date[] startTimes = GovdocStatHelper.getStartAndEndTime(displayTimeType, displayId);
        	buffer.append(" and (summary.startTime between :startTimeC and :startTimeE)");
        	parameterMap.put("startTimeC", startTimes[0]);
        	parameterMap.put("startTimeE", startTimes[1]);
    	}
		return buffer.toString();
	}
	
	/**
	 * 公文范围SQL
	 * @param deptIdList
	 * @param memberIdList
	 * @param parameterMap
	 * @return
	 */
	private String getStatRangeHql(List<Long> deptIdList, List<Long> memberIdList, Map<String, Object> parameterMap) {
		if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	return " and OrgMember.orgDepartmentId in (:deptIdList)";
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	return " and OrgMember.id in (:memberIdList)";
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	return " and (OrgMember.orgDepartmentId in (:deptIdList) or OrgMember.id in (:memberIdList))";
        }
		return "";
	}
	
	/**
	 * 公文已发穿透范围SQL
	 * @param deptIdList
	 * @param memberIdList
	 * @param parameterMap
	 * @return
	 */
	private String getSentStatRangeHql(List<Long> deptIdList, List<Long> memberIdList, Map<String, Object> parameterMap) {
		if(Strings.isNotEmpty(deptIdList) && Strings.isEmpty(memberIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	return " and summary.orgDepartmentId in (:deptIdList)";
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isEmpty(deptIdList)) {
        	parameterMap.put("memberIdList", memberIdList);
        	return " and summary.startUserId in (:memberIdList)";
        } else if(Strings.isNotEmpty(memberIdList) && Strings.isNotEmpty(deptIdList)) {
        	parameterMap.put("deptIdList", deptIdList);
        	parameterMap.put("memberIdList", memberIdList);
        	return " and (summary.orgDepartmentId in (:deptIdList) or  summary.startUserId in (:memberIdList))";
        }
		return "";
	}
	
	/**
	 * 统计条件筛选
	 * @param params
	 * @param parameterMap
	 * @return
	 */
	private String getSummaryByConditionHql(Map<String, String> params, Map<String, Object> parameterMap) {
		String unitLevelId = (String) params.get("unitLevelId");
		String sendTypeId = (String) params.get("sendTypeId");
		Date startTime = params.get("startRangeTime")==null ? null : Datetimes.parseDatetime(params.get("startRangeTime")+" 00:00:00");
		Date endTime = params.get("endRangeTime")==null ? null : Datetimes.parseDatetime(params.get("endRangeTime")+" 23:59:59");
		String operationTypeIdsStr = (String) params.get("operationTypeIds");
		String operationType = (String) params.get("operationType");
		String[] operationTypeIds = null;
    	if(operationTypeIdsStr!=null&&!"".equals(operationTypeIdsStr.trim())){
    		operationTypeIds = operationTypeIdsStr.split(",");
    	}
    	StringBuilder tempSQL = new StringBuilder();
		if(Strings.isNotBlank(unitLevelId)) {
        	List<String> unitLevelIdList = new ArrayList<String>();
        	for(String unitLevel : unitLevelId.split(",")) {
        		unitLevelIdList.add(unitLevel);
        	}
        	tempSQL.append(" and summary.unitLevel in (:unitLevelList)");
        	parameterMap.put("unitLevelList", unitLevelIdList);
        }
        if(Strings.isNotBlank(sendTypeId)) {
        	List<String> sendTypeList = new ArrayList<String>();
        	for(String sendType : sendTypeId.split(",")) {
        		sendTypeList.add(sendType);
        	}
        	tempSQL.append(" and summary.sendType in (:sendTypeList)");
        	parameterMap.put("sendTypeList", sendTypeList);
        }
        if(startTime!=null && endTime!=null) {
        	tempSQL.append(" and (summary.createTime between :startTime and :endTime)");
        	parameterMap.put("startTime", startTime);
            parameterMap.put("endTime", endTime);
        }
        StringBuilder templateHSQL = new StringBuilder();
        if(Strings.isNotBlank(operationType)) {
        	boolean sysTemplateNotNull = operationTypeIds!=null && !"".equals(operationTypeIds[0]);
        	if(operationType.contains("template")) {//系统模板
		        if(sysTemplateNotNull) {//模板过滤不为空
		        	templateHSQL.append(" and (");
		        	templateHSQL.append(" summary.templeteId in (");
		    		for(int i=0; i<operationTypeIds.length; i++){
		        		Long templeteId = Long.parseLong(operationTypeIds[i]);
		        		if(i == operationTypeIds.length-1) {
		        			templateHSQL.append(templeteId);
		        		} else {
		        			templateHSQL.append(templeteId).append(",");
		        		}
		    		}
		    		templateHSQL.append(")");
		    		if(operationType.contains("self")) {
		    			templateHSQL.append(" or summary.templeteId is null");
		    		}
		    		templateHSQL.append(")");
		        } else {
		        	if(!operationType.contains("self")) { 
		        		templateHSQL.append(" and summary.templeteId is not null");
		        	}
		        }
        	} 
        	else if(operationType.contains("self")) {//自由流程  		
        		templateHSQL.append(" and summary.templeteId is null");
        	}
        } else {
    		templateHSQL.append(" and summary.templeteId = :templeteId");//因为1=2索引会失效，这里使用一个非正常值
    		parameterMap.put("templeteId", -1);
    	}
    	tempSQL.append(templateHSQL);
		 return tempSQL.toString();
	}
	
	/**
	 * 获取某公文大字段数据
	 * @param summaryIdList
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> getEdocLongtextFields(List<Long> summaryIdList) throws BusinessException {
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		List<List<Long>> temp = new ArrayList<List<Long>>();
		if(null != summaryIdList && summaryIdList.size() >= 1000){//兼容oracle下 1000的限制 BUG_普通_V5_V5.6SP1_广西机场管理集团
			int index = 0;
			int subSize = summaryIdList.size()/1000+1;
			for(int i=0;i<subSize;i++){
				temp.add(summaryIdList.subList(index, index+999 > summaryIdList.size() ?  summaryIdList.size() : index+999));
				index = index+999;
			}
			StringBuilder sb = new StringBuilder();
			for(int i=0;i<temp.size();i++){
				parameterMap.put("summaryIdList"+i, temp.get(i));
				sb.append(" id in (:summaryIdList").append(i).append(") or");
			}
			return DBAgent.find("select id, sendUnit, currentNodesInfo, undertakenoffice from EdocSummary where "+sb.substring(0, sb.length()-2), parameterMap);
		}else{
			parameterMap.put("summaryIdList", summaryIdList);
			return DBAgent.find("select id, sendUnit, currentNodesInfo, undertakenoffice from EdocSummary where id in (:summaryIdList)", parameterMap);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Object findBySQL(final String sql, final Map<String, Object> parameterMap) {
		return (Object) super.getHibernateTemplate().execute(new HibernateCallback() {			
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				SQLQuery query = session.createSQLQuery(sql);
				setParameter(parameterMap, query);
				List list = query.list();
				return list;
			}
		});
	}
	
	@SuppressWarnings("rawtypes")
	private static void setParameter(Map<String, Object> parameter, Query query) {
		if (parameter != null) {
			Set<Map.Entry<String, Object>> entries = parameter.entrySet();
			for (Map.Entry<String, Object> entry : entries) {
				String name = entry.getKey();
				Object value = entry.getValue();
				if(value instanceof Collection){
					query.setParameterList(name, (Collection)value);
				} else if(value instanceof Object[]){
					query.setParameterList(entry.getKey(), (Object[])value);
				} else{
					query.setParameter(entry.getKey(), value);
				}
			}
		}
	}

	/************************************************** 公文统计Hql(备用方法) start ***************************************************************/
	/**
	 * 公文统计-经办(Hql)
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findEdocStatResultByHql(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		User user = AppContext.getCurrentUser();
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		boolean isCurrentDeptExchange = GovdocRoleHelper.isDepartmentExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		Map<String, Object> parameterMap = new HashMap<String, Object>();
		StringBuilder buffer = new StringBuilder();
        buffer.append("select "+selectStatResult+",OrgMember.orgDepartmentId from EdocStat stat, CtpAffair affair, OrgMember OrgMember ");
        buffer.append(" where stat.edocId=affair.objectId and affair.memberId=OrgMember.id");
        buffer.append(getStatRangeHql(deptIdList, memberIdList, parameterMap));
        buffer.append(" and OrgMember.orgAccountId = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        if(!isCurrentAccountExchange && isCurrentDeptExchange) {
	        buffer.append(" and OrgMember.orgDepartmentId in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        buffer.append(" and stat.edocType = :edocType and affair.app = :app");
        parameterMap.put("edocType", edocType);
        parameterMap.put("app", GovdocStatHelper.getEdocApp(edocType));
        
        buffer.append(" and (affair.state=:state1 or affair.state=:state2)");
        parameterMap.put("state1", StateEnum.col_pending.key());
        parameterMap.put("state2", StateEnum.col_done.key());
        
        buffer.append(getStatByConditionHql(params, parameterMap));
        int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
		if(displayType == EdocStatDisplayTypeEnum.department.key()) {//按部门
			buffer.append(" group by OrgMember.orgDepartmentId,stat.edocId,affair.state");
		} else if(displayType == EdocStatDisplayTypeEnum.member.key()) {//按人
			buffer.append(" group by OrgMember.id,stat.edocId,affair.state");
		} else {//按时间
			if(displayTimeType == EdocStatDisplayTimeTypeEnum.year.key()) {//年
				buffer.append(" group by stat.year,stat.edocId,affair.state");	
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.quarter.key()) {//季
				buffer.append(" group by stat.year,stat.quarter,stat.edocId,affair.state");	
			} else if(displayTimeType == EdocStatDisplayTimeTypeEnum.month.key()) {//月
				buffer.append(" group by stat.year,stat.month,stat.edocId,affair.state");	
			} else {
				buffer.append(" group by stat.year,stat.month,stat.day,stat.edocId,affair.state");
			}
		}
        List<Object[]> list = DBAgent.find(buffer.toString(), parameterMap);
        return list;
	}
		
	/**
	 * 公文统计-已发(Hql)
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<Object[]> findSentEdocStatResultByHql(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		User user = AppContext.getCurrentUser();
		boolean isCurrentAccountExchange = GovdocRoleHelper.isAccountExchange();
		int edocType = params.get("edocType")==null ? 0 : Integer.parseInt(params.get("edocType"));
		List<Long> userSelfAndSubDeptIdList = GovdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> deptIdList = GovdocStatHelper.getRangeIdList(params, "Department");
		//去重处理
		GovdocStatHelper.removeRepeatItem(deptIdList);
		List<Long> memberIdList = GovdocStatHelper.getRangeIdList(params, "Member");
		Map<String, Object> parameterMap = new HashMap<String, Object>();		
        StringBuilder buffer = new StringBuilder();
        buffer.append("select distinct " + selectStatSentResultSummary + " from EdocSummary summary, CtpAffair affair");
        buffer.append(" where summary.id=affair.objectId and summary.edocType = :edocType");
        parameterMap.put("edocType", edocType);
        buffer.append(getSentStatRangeHql(deptIdList, memberIdList, parameterMap));
        buffer.append(" and summary.orgAccountId = :currentAccountId");
        parameterMap.put("currentAccountId", user.getLoginAccount());
        if(!isCurrentAccountExchange && Strings.isNotEmpty(userSelfAndSubDeptIdList)) {
	        buffer.append(" and summary.orgDepartmentId in (:userSelfAndSubDeptIdList)");
			parameterMap.put("userSelfAndSubDeptIdList", userSelfAndSubDeptIdList);
        }
        buffer.append(" and affair.state = 2");
        buffer.append(getSummaryByConditionHql(params, parameterMap));
        List<Object[]> list = DBAgent.find(buffer.toString(), parameterMap);  
        return list;
	}
	

	/**
	 * 统计条件筛选
	 * @param params
	 * @param parameterMap
	 * @return
	 */
	private String getStatByConditionHql(Map<String, String> params, Map<String, Object> parameterMap) {
		String unitLevelId = (String) params.get("unitLevelId");
		String sendTypeId = (String) params.get("sendTypeId");
		Date startTime = params.get("startRangeTime")==null ? null : Datetimes.parseDatetime(params.get("startRangeTime")+" 00:00:00");
		Date endTime = params.get("endRangeTime")==null ? null : Datetimes.parseDatetime(params.get("endRangeTime")+" 23:59:59");
		String operationTypeIdsStr = (String) params.get("operationTypeIds");
		String operationType = (String) params.get("operationType");
		String[] operationTypeIds = null;
    	if(operationTypeIdsStr!=null&&!"".equals(operationTypeIdsStr.trim())){
    		operationTypeIds = operationTypeIdsStr.split(",");
    	}
    	StringBuilder tempSQL = new StringBuilder();
		if(Strings.isNotBlank(unitLevelId)) {
        	List<String> unitLevelIdList = new ArrayList<String>();
        	for(String unitLevel : unitLevelId.split(",")) {
        		unitLevelIdList.add(unitLevel);
        	}
        	tempSQL.append(" and stat.unitLevel in (:unitLevelList)");
        	parameterMap.put("unitLevelList", unitLevelIdList);
        }
        if(Strings.isNotBlank(sendTypeId)) {
        	List<String> sendTypeList = new ArrayList<String>();
        	for(String sendType : sendTypeId.split(",")) {
        		sendTypeList.add(sendType);
        	}
        	tempSQL.append(" and stat.sendType in (:sendTypeList)");
        	parameterMap.put("sendTypeList", sendTypeList);
        }
        if(startTime!=null && endTime!=null) {
        	tempSQL.append(" and (stat.createDate between :startTime and :endTime)");
        	parameterMap.put("startTime", startTime);
            parameterMap.put("endTime", endTime);
        }
        String templateHSQL = "";
        if(Strings.isNotBlank(operationType)) {
        	boolean sysTemplateNotNull = operationTypeIds!=null && !"".equals(operationTypeIds[0]);
        	if(operationType.contains("template")) {//系统模板
		        if(sysTemplateNotNull) {//模板过滤不为空
		        	templateHSQL += " and (";
		        	templateHSQL += " stat.templeteId in (";
		    		for(int i=0; i<operationTypeIds.length; i++){
		        		Long templeteId = Long.parseLong(operationTypeIds[i]);
		        		if(i == operationTypeIds.length-1) {
		        			templateHSQL += templeteId;
		        		} else {
		        			templateHSQL += templeteId+",";
		        		}
		    		}
		    		templateHSQL += ")";
		    		if(operationType.contains("self")) {
		    			templateHSQL += " or stat.templeteId is null";
		    		}
		    		templateHSQL += ")";
		        } else {
		        	if(!operationType.contains("self")) { 
		        		templateHSQL += " and stat.templeteId is not null";
		        	}
		        }
        	} 
        	else if(operationType.contains("self")) {//自由流程  		
        		templateHSQL += " and stat.templeteId is null";
        	}
        
        } else {
        	templateHSQL += " and stat.templeteId = :templeteId";//因为1=2索引会失效，这里使用一个非正常值
    		parameterMap.put("templeteId", -1);
    	}
    	tempSQL.append(templateHSQL);
		 return tempSQL.toString();
	}
	
	/*************************************************** 公文统计Hql(备用方法) end ***************************************************************/

}
