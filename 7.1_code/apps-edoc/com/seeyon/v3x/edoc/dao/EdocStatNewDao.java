package com.seeyon.v3x.edoc.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

import com.seeyon.apps.govdoc.helper.GovdocRoleHelper;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.authenticate.domain.User;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocStat;
import com.seeyon.v3x.edoc.manager.EdocSwitchHelper;
import com.seeyon.v3x.edoc.util.EdocStatEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTimeTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatDisplayTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatEnum.EdocStatListTypeEnum;
import com.seeyon.v3x.edoc.util.EdocStatHelper;

public class EdocStatNewDao extends BaseHibernateDao<EdocStat> {

	private static final String selectStatSentResultSummary = "summary.id,summary.templeteId,summary.edocType," +
	        "summary.subject,summary.sendDepartmentId,summary.state,summary.startTime, summary.orgDepartmentId, summary.startUserId, summary.orgAccountId" ;
	
	private static final String selectStatResult = "stat.edocType,stat.flowState,stat.createDate,affair.objectId,affair.subject,affair.memberId,affair.app,affair.subApp,affair.nodePolicy,affair.state,affair.subState, count(affair.state) as countAffairState,count(affair.subState) as countAffairSubState";
	
	private static String selectStatListSummary = "summary.id,summary.subject, summary.docMark, summary.serialNo, summary.state, summary.startUserId, summary.deadlineDatetime, summary.deadline, summary.hasArchive, summary.sendDepartment,summary.unitLevel,summary.issuer, summary.coverTime, summary.orgAccountId, summary.startTime, summary.archiveId, summary.templeteId, summary.signingDate, summary.completeTime,  summary.registrationDate";	
	
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
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		EdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		Integer state = EdocStatHelper.getResultAffairState(resultType);
		Integer subApp = EdocStatHelper.getResultAffairSubApp(resultType);
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
        buffer.append(" and affair.app = :app");
        parameterMap.put("app", EdocStatHelper.getEdocApp(edocType));
        //公文子类型
        if(subApp != null) {
        	boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
	        if(showBanwenYuewen) {
                if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {
                    //老数据sub_app可能为空，也可能是-1
                    buffer.append(" and (affair.sub_app = :subApp or affair.sub_app is null or affair.sub_app = :oldApp)");
                    parameterMap.put("oldApp", -1);
                } else {
                    buffer.append(" and affair.sub_app = :subApp");
                }
                parameterMap.put("subApp", subApp);
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
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		EdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
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
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		EdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		int resultType = Strings.isBlank(params.get("resultType")) ? -1 : Integer.parseInt(params.get("resultType"));
		int displayType = Strings.isBlank(params.get("displayType")) ? EdocStatDisplayTypeEnum.department.key() : Integer.parseInt(params.get("displayType"));
        int displayTimeType = Strings.isBlank(params.get("displayTimeType")) ? EdocStatDisplayTimeTypeEnum.year.key() : Integer.parseInt(params.get("displayTimeType"));
        //获取统计部分sql
        String sqlColumnAsDisplayId = getTableColumnAsDisplayIdByDisplayType(displayType, displayTimeType, resultType);
        String sqlColumnAs = getTableColumnAsByResultType(resultType);
        //拼装统计sql
        StringBuilder buffer = new StringBuilder();
		buffer.append(" select distinct " + sqlColumnAsDisplayId + "," + sqlColumnAs);
        buffer.append(" from edoc_stat stat, ctp_affair affair, org_member OrgMember");
		buffer.append(" where affair.member_id=OrgMember.id and stat.edoc_id=affair.object_id");
		//统计公文类型
		buffer.append(" and stat.edoc_type = :edocType");
		parameterMap.put("edocType", edocType);
        //处理状态
        List<Integer> stateList = EdocStatHelper.getResultAffairStateList(resultType);
        List<Integer> subStateList = EdocStatHelper.getResultAffairSubStateList(resultType);
        Integer subApp = EdocStatHelper.getResultAffairSubApp(resultType);
        if(Strings.isNotEmpty(stateList)) {
        	buffer.append(" and affair.state in (:stateList)");
        	parameterMap.put("stateList", stateList);
        }
        if(Strings.isNotEmpty(subStateList)) {
        	buffer.append(" and affair.sub_state in (:subStateList)");
        	parameterMap.put("subStateList", subStateList);
        }
        //公文类型
        buffer.append(" and affair.app = :app");
        parameterMap.put("app", EdocStatHelper.getEdocApp(edocType));
        //公文子类型
        boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
        if(subApp != null) {
	        if(showBanwenYuewen) {
                if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {
                    //老数据sub_app可能为空，也可能是-1
                    buffer.append(" and (affair.sub_app = :subApp or affair.sub_app is null or affair.sub_app = :oldApp)");
                    parameterMap.put("oldApp", -1);
                } else {
                    buffer.append(" and affair.sub_app = :subApp");
                }
                parameterMap.put("subApp", subApp);
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
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");		
		//去重复处理
		EdocStatHelper.removeRepeatItem(deptIdList);
		//自己有交换权限的所有部门
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Integer> stateList = EdocStatHelper.getResultAffairStateList(resultType);
		Integer subApp = EdocStatHelper.getResultAffairSubApp(resultType);
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
        buffer.append(" and affair.app = :app");
        parameterMap.put("app", EdocStatHelper.getEdocApp(edocType));
        //公文子类型
        if(subApp != null) {
        	boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
	        if(showBanwenYuewen) {
                if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {
                    //老数据sub_app可能为空，也可能是-1
                    buffer.append(" and (affair.sub_app = :subApp or affair.sub_app is null or affair.sub_app = :oldApp)");
                    parameterMap.put("oldApp", -1);
                } else {
                    buffer.append(" and affair.sub_app = :subApp");
                }
                parameterMap.put("subApp", subApp);
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
	 * 
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
    		buffer.append(" where summary.edocType = :edocType");
    		
    		//已发或者指定回退-直接回退给我
    		buffer.append(" and (affair.state = :aState or (affair.state=:aState1 and affair.subState = :aSubState))");
    		buffer.append(" and summary.id=affair.objectId");
    		
    		parameterMap.put("aState", StateEnum.col_sent.getKey());
    		parameterMap.put("aState1", StateEnum.col_waitSend.getKey());
    		parameterMap.put("aSubState", SubStateEnum.col_pending_specialBacked.getKey());
    		
    		
    		parameterMap.put("edocType", edocType);
    		buffer.append(getStatSentEdocListHQL(params, parameterMap));
        } else {//办理
        	buffer.append("select distinct "+selectStatListSummary + " from EdocSummary summary, CtpAffair affair, OrgMember OrgMember");
            buffer.append(" where summary.id=affair.objectId and affair.memberId=OrgMember.id");
            /** 当前接收人 */
            buffer.append(" and summary.edocType = :edocType and affair.app = :app");
            parameterMap.put("edocType", edocType);
            parameterMap.put("app", EdocStatHelper.getEdocApp(edocType));
        	buffer.append(getStatDoneEdocListHQL(params, parameterMap));
        	List<Integer> stateList = EdocStatHelper.gerAffairStateList(listType);
            List<Integer> subStateList = EdocStatHelper.gerAffairSubStateList(listType);
            Integer subApp = EdocStatHelper.gerAffairSubApp(listType);
            
            if(Strings.isNotEmpty(stateList)) {
            	buffer.append(" and affair.state in (:stateList)");
            	parameterMap.put("stateList", stateList);
            }
            if(Strings.isNotEmpty(subStateList)) {
            	buffer.append(" and affair.subState in (:subStateList)");
            	parameterMap.put("subStateList", subStateList);
            }
            if(subApp != null) {
            	boolean showBanwenYuewen = EdocSwitchHelper.showBanwenYuewen(AppContext.getCurrentUser().getLoginAccount());
    	        if(showBanwenYuewen) {
                    if(subApp.intValue()==ApplicationSubCategoryEnum.edocRecHandle.key()) {
                        //老数据sub_app可能为空，也可能是-1
                        buffer.append(" and (affair.subApp = :subApp or affair.subApp is null or affair.subApp = :oldApp)");
                        parameterMap.put("oldApp", -1);
                    } else {
                        buffer.append(" and affair.subApp = :subApp");
                    }
                    parameterMap.put("subApp", subApp);
                }
            }
        }
        List<Integer> summaryStateList = EdocStatHelper.getSummaryStateList(listType);
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
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> selectDeptIdList = EdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = EdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");
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
    		Date[] startTimes = EdocStatHelper.getStartAndEndTime(displayTimeType, displayId);
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
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> selectDeptIdList = EdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Department");
		List<Long> selectMemberDeptIdList = EdocStatHelper.getSelectedDeptIdList(params.get("rangeIds"), "Member");
		if(Strings.isNotEmpty(selectDeptIdList) && Strings.isNotEmpty(selectMemberDeptIdList)) {
			selectDeptIdList.addAll(selectMemberDeptIdList);
		}
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");
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
    		Date[] startTimes = EdocStatHelper.getStartAndEndTime(displayTimeType, displayId);
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
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
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
        parameterMap.put("app", EdocStatHelper.getEdocApp(edocType));
        
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
		List<Long> userSelfAndSubDeptIdList = EdocStatHelper.getCurrentUserAllExchangeDeptIdList();
		List<Long> deptIdList = EdocStatHelper.getRangeIdList(params, "Department");
		//去重处理
		EdocStatHelper.removeRepeatItem(deptIdList);
		List<Long> memberIdList = EdocStatHelper.getRangeIdList(params, "Member");
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
