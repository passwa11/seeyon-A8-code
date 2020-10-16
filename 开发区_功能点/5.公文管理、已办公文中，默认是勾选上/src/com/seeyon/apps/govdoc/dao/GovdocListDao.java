package com.seeyon.apps.govdoc.dao;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.seeyon.apps.edoc.constants.EdocConstant;
import com.seeyon.apps.edoc.constants.EdocConstant.flowState;
import com.seeyon.apps.govdoc.constant.GovdocEnum.GovdocExchangeTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OldEdocTypeEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.TransferStatus;
import com.seeyon.apps.govdoc.constant.GovdocListEnum.GovdocListTypeEnum;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocOrgHelper;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.constants.ApplicationSubCategoryEnum;
import com.seeyon.ctp.common.content.affair.constants.StateEnum;
import com.seeyon.ctp.common.content.affair.constants.SubStateEnum;
import com.seeyon.ctp.common.dao.BaseHibernateDao;
import com.seeyon.ctp.common.dump.vo.DumpDataVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.flag.SysFlag;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.form.util.Enums.FormType;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.Datetimes;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.SQLWildcardUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;
import com.seeyon.v3x.edoc.listener.EdocWorkflowManageHandler;

public class GovdocListDao extends BaseHibernateDao{

    private OrgManager orgManager = null;

    public void setOrgManager(OrgManager orgManager) {
        this.orgManager = orgManager;
    }

    //公文列表主表Hql
	private static final String SummaryListHql = "summary.id," +
		        "summary.subject," +
		        "summary.identifier," +
		        "summary.govdocType," +
		        "summary.state," +
			    "summary.isQuickSend," +
		        "summary.templeteId," +
		        "summary.archiveId," +
		        "summary.processId," +
		        "summary.caseId," +
		        "summary.startUserId," +//11
		        "summary.startTime," +
		        "summary.createTime," +
		        "summary.completeTime," +
		        "summary.deadline," +
		        "summary.deadlineDatetime," +
		        "summary.coverTime," +
			    "summary.docMark,"+
			    "summary.serialNo,"+
			    "summary.docMark2,"+
			    "summary.docType,"+//21
			    "summary.sendType,"+
			    "summary.secretLevel,"+
			    "summary.urgentLevel,"+
			    "summary.unitLevel,"+
			    "summary.keepPeriod,"+
			    "summary.copies,"+
			    "summary.issuer,"+
			    "summary.signingDate,"+
			    "summary.createPerson,"+
			    "summary.printer,"+//31
			    "summary.phone,"+
			    "summary.orgAccountId,"+
			    "summary.exchangeSendAffairId,"+
			    "summary.undertaker," +
			    "summary.undertakenoffice," +
			    "summary.bodyType," +
			    "summary.hasArchive,"+
			    "summary.signPerson,"+
			    "summary.receiptDate,"+
			    "summary.registrationDate,"+//41
			    "summary.fromType,"+
			    "summary.newflowType";

	//公文列表主表Clob字段Hql
	private static final String SummaryColbListHql = "summary.currentNodesInfo, "
			+ "summary.sendTo, "
			+ "summary.copyTo, "
			+ "summary.reportTo, "
			+ "summary.printUnit, "
			+ "summary.sendUnit, "
			+ "summary.sendDepartment";

	//公文列表待办Hql
	private static final String AffariListHql = "affair.id," +
	        "affair.identifier,"+
	        "affair.subject,"+
	        "affair.app,"+
		    "affair.subApp,"+
	        "affair.state," +
	        "affair.subState," +
	        "affair.finish," +
	        "affair.objectId,"+
	        "affair.subObjectId," +
	        "affair.activityId,"+
	        "affair.nodePolicy," +
	        "affair.memberId," +
	        "affair.transactorId," +

	        "affair.hastenTimes," +
	        "affair.coverTime," +
	        "affair.remindDate," +
	        "affair.deadlineDate," +
	        "affair.receiveTime," +
	        "affair.completeTime," +
	        "affair.createDate," +
	        "affair.updateDate," +
	        "affair.expectedProcessTime," +
	        "affair.importantLevel," +
		    "affair.archiveId,"+
	        "affair.track," +
		    "affair.backFromId,"+
		    "affair.preApprover,"+
		    "affair.proxyMemberId,"+
		    "affair.transactorId,"+"affair.senderId";

	//公文登记列表主表字段Hql(非Clob)
	private static final String RegSummaryHql = "summary.id"
			+ ",summary.subject"
			+ ",summary.identifier"
			+ ",summary.govdocType"
			+ ",summary.edocType"
			+ ",summary.state"
			+ ",summary.transferStatus"
			+ ",summary.sendType"
			+ ",summary.docType"
			+ ",summary.unitLevel"//10
			+ ",summary.urgentLevel"
			+ ",summary.secretLevel"
			+ ",summary.edocSecretLevel"
			+ ",summary.keepPeriod"
			+ ",summary.createTime"
			+ ",summary.completeTime"
			+ ",summary.packTime"
			+ ",summary.receiptDate"
			+ ",summary.registrationDate"
			+ ",summary.startTime"//20
			+ ",summary.startUserId"
			+ ",summary.orgAccountId"
			+ ",summary.orgDepartmentId"
			+ ",summary.docMark"
			+ ",summary.docMark2"
			+ ",summary.serialNo"
			+ ",summary.copies"
			+ ",summary.copies2"
			+ ",summary.createPerson"
			+ ",summary.review"//30
			+ ",summary.issuer"
			+ ",summary.signPerson"
			+ ",summary.signingDate"
			+ ",summary.bodyType"
	        + ",summary.undertaker"
            + ",summary.exchangeSendAffairId";

	//公文登记列表Clob字段Hql(Clob)
	private static final String RegClobSummaryHql = "summary.sendTo"
			+ ",summary.sendTo2"
			+ ",summary.sendToId"
			+ ",summary.sendToId2"
			+ ",summary.copyTo"
			+ ",summary.copyTo2"
			+ ",summary.copyToId"
			+ ",summary.copyToId2"
			+ ",summary.reportTo"
			+ ",summary.reportTo2"//10
			+ ",summary.reportToId"
			+ ",summary.reportToId2"
			+ ",summary.sendUnit"
			+ ",summary.sendUnitId"
			+ ",summary.sendUnit2"
			+ ",summary.sendUnitId2"
			+ ",summary.sendDepartment"
			+ ",summary.sendDepartmentId"
			+ ",summary.sendDepartment2"//20
			+ ",summary.sendDepartmentId2"
			+ ",summary.currentNodesInfo";

	private static final String AdminManagerHql = "affair.subApp,summary.id,affair.app,summary.subject,summary.startUserId,u.name,summary.createTime,summary.processId,summary.caseId, "
																		+ " affair.deadlineDate,affair.remindDate,summary.orgAccountId,affair.finish,summary.templeteId,summary.completeTime,summary.deadlineDatetime,summary.currentNodesInfo "
																		+ ", summary.secretLevel,affair.formAppId,affair.formRecordid ";

	//公文登记列表登记字段Hql
	private static final String RegRecHql = "register.id";

	//公文列表/公文查询/关联公文-所有字段Hql
	private static final String ListAllColumnHql = SummaryListHql + "," + SummaryColbListHql + "," + AffariListHql;

	//发文/签报登记簿-所有字段Hql
	private static final String RegisterAllColumnHql = RegSummaryHql + "," + RegClobSummaryHql;

	//收文登记簿-所有字段Hql
	private static final String RecRegisterAllColumnHql = RegSummaryHql + "," + RegClobSummaryHql + "," + RegRecHql;

	private static final String BackExchangeHql = "summary.id,summary.docMark,summary.completeTime,summary.subject,log.backOpinion,detail.sendAccountId,detail.sendUnit,detail.recOrgName";

	private static final String queryOldDataIdByNewDataId = "select oldDataId from UpgradeDataRelation where newDataId in (:newTids)";

	/**
	 * 公文列表数据展及查询
	 * 用于二级菜单-待办/已办/已发/待发 发文管理/收文管理/签报管理
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();

		String listType = params.get("listType");
		String govdocType = params.get("govdocType");
		Long memberId = Long.parseLong(params.get("memberId"));
		boolean hasDeduplication = "true".equals(params.get("deduplication"));
		boolean hasAgent = "true".equals(params.get("agentFlag")) || "true".equals(params.get("agentToFlag"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id = affair.objectId");

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, memberId, hasAgent));
		//公文app及subApp
		buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, true));
		//处理状态
		buffer.append(getHqlByAffairState(paramMap, listType));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按节点权限(非)过滤
		buffer.append(getHqlByAffairNotInPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
			buffer.append(getHqlBySummaryTemplateIds(paramMap, params));//公文模板ID查询-来自业务生成器
			buffer.append(getAffairHqlByCondition(paramMap, params));//按affair表字段查询
		}

		if(hasDeduplication) {
//			buffer.append(" and affair.id = (");
//			zhou
			buffer.append(" and affair.completeTime = (");
//			zhou:修改获取处理时间最大的数据
			buffer.append(" 	select max(affair2.completeTime) from CtpAffair affair2");
			buffer.append("  	where affair2.objectId = summary.id");
			buffer.append("  	and affair2.subApp = affair.subApp");
			//公文处理人员过滤(包括代理人)
			buffer.append(getHqlByAffairMember(paramMap, memberId, hasAgent, "affair2"));
			//公文app及subApp
			buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, true, "affair2"));
			//处理状态`
			buffer.append(getHqlByAffairState(paramMap, listType, "affair2"));
			//按节点权限过滤
			buffer.append(getHqlByAffairPolicy(paramMap, params, "affair2"));
			//按节点权限(非)过滤
			buffer.append(getHqlByAffairNotInPolicy(paramMap, params, "affair2"));
			//按条件查询
			if(params.containsKey("condition")) {
				buffer.append(getAffairHqlByCondition(paramMap, params, "affair2"));//按affair表字段查询
			}
			buffer.append(" )");
		}
		if(listType != null){
			if(listType.startsWith("listDone")) {//已办
				buffer.append(" order by affair.completeTime desc");
			} else if(listType.startsWith("listPending")) {//待办
				buffer.append(" order by affair.receiveTime desc");
			} else if(listType.startsWith("listWaitSend")) {//待发
				buffer.append(" order by summary.createTime desc");
			} else {
				buffer.append(" order by summary.createTime desc");
			}
		}

		boolean dumpData = DumpDataVO.dataType.dumpData.getKey().equals(params.get("dumpData"));

		List<Object[]> result = null;
		if(flipInfo != null && !dumpData) // 查询转储数据必须要用super的find相关方法才能查询分库
		{
			result = DBAgent.find(buffer.toString(), paramMap,flipInfo);
		}
		else
		{
			result = super.find(buffer.toString(), paramMap);
		}


		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, hasDeduplication);
				voList.add(vo);
			}
		}

		return voList;
	}
	/**
	 * 公文列表数据总条数查询
	 * 用于二级菜单-待办/已办/已发/待发 发文管理/收文管理/签报管理
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public int findListCount(Map<String, String> params) throws BusinessException {
		int resultCount = 0;
		String listType = params.get("listType");
		String govdocType = params.get("govdocType");
		Long memberId = Long.parseLong(params.get("memberId"));
		boolean hasDeduplication = "true".equals(params.get("deduplication"));
		boolean hasAgent = "true".equals(params.get("agentFlag")) || "true".equals(params.get("agentToFlag"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select count(summary.id) from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id = affair.objectId");

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, memberId, hasAgent));
		//公文app及subApp
		buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, true));
		//处理状态
		buffer.append(getHqlByAffairState(paramMap, listType));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按节点权限(非)过滤
		buffer.append(getHqlByAffairNotInPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
			buffer.append(getHqlBySummaryTemplateIds(paramMap, params));//公文模板ID查询-来自业务生成器
			buffer.append(getAffairHqlByCondition(paramMap, params));//按affair表字段查询
		}

		if(hasDeduplication) {
			buffer.append(" and affair.completeTime = (");
			buffer.append(" 	select max(affair2.completeTime) from CtpAffair affair2");
			buffer.append("  	where affair2.objectId = summary.id");
			//公文处理人员过滤(包括代理人)
			buffer.append(getHqlByAffairMember(paramMap, memberId, hasAgent, "affair2"));
			//公文app及subApp
			buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, true, "affair2"));
			//处理状态
			buffer.append(getHqlByAffairState(paramMap, listType, "affair2"));
			//按节点权限过滤
			buffer.append(getHqlByAffairPolicy(paramMap, params, "affair2"));
			//按节点权限(非)过滤
			buffer.append(getHqlByAffairNotInPolicy(paramMap, params, "affair2"));
			//按条件查询
			if(params.containsKey("condition")) {
				buffer.append(getAffairHqlByCondition(paramMap, params, "affair2"));//按affair表字段查询
			}
			buffer.append(" )");
		}
		if(listType != null){
			if(listType.startsWith("listDone")) {//已办
				buffer.append(" order by affair.completeTime desc");
			} else if(listType.startsWith("listPending")) {//待办
				buffer.append(" order by affair.receiveTime desc");
			} else if(listType.startsWith("listWaitSend")) {//待发
				buffer.append(" order by summary.createTime desc");
			} else {
				buffer.append(" order by summary.createTime desc");
			}
		}

		resultCount = DBAgent.count(buffer.toString(), paramMap);
		return resultCount;
	}

	/**
	 * 公文关联文档数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> find4QuoteList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();

		String listType = params.get("listType");
		String govdocType = params.get("govdocType");
		Long memberId = Long.parseLong(params.get("memberId"));
		boolean hasDeduplication = "true".equals(params.get("deduplication"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id = affair.objectId");

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, memberId, false));
		//公文app及subApp
		buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, false));
		//处理状态
		buffer.append(getHqlByAffairState(paramMap, listType));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
			buffer.append(getAffairHqlByCondition(paramMap, params));//按affair表字段查询
		}

		if(hasDeduplication) {
			buffer.append(" and affair.completeTime = (");
			buffer.append(" 	select max(affair2.completeTime) from CtpAffair affair2");
			buffer.append("  	where affair2.objectId = summary.id");
			//公文处理人员过滤(包括代理人)
			buffer.append(getHqlByAffairMember(paramMap, memberId, false, "affair2"));
			//公文app及subApp
			buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, false, "affair2"));
			//处理状态
			buffer.append(getHqlByAffairState(paramMap, listType, "affair2"));
			//按节点权限过滤
			buffer.append(getHqlByAffairPolicy(paramMap, params, "affair2"));
			//按条件查询
			if(params.containsKey("condition")) {
				buffer.append(getAffairHqlByCondition(paramMap, params, "affair2"));//按affair表字段查询
			}
			buffer.append(" )");
		}
		if(listType != null){
			if(listType.startsWith("listDone")) {//已办
				buffer.append(" order by affair.completeTime desc");
			} else if(listType.startsWith("listPending")) {//待办
				buffer.append(" order by affair.receiveTime desc");
			} else if(listType.startsWith("listWaitSend")) {//待发
				buffer.append(" order by summary.createTime desc");
			} else {
				buffer.append(" order by summary.createTime desc");
			}
		}
		if(flipInfo != null) {
			flipInfo.setNeedTotal(true);
		}
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, hasDeduplication);
				voList.add(vo);
			}
		}

		return voList;
	}

	/**
	 * 公文二级菜单-公文查询数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findQueryResultList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();

		String listType = params.get("listType");
		String govdocType = params.get("govdocType");
		Long memberId = Long.parseLong(params.get("memberId"));
		boolean hasDeduplication = "true".equals(params.get("deduplication"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id = affair.objectId");

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, memberId, false));
		//公文app及subApp
		buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, false));
		//处理状态
		buffer.append(getHqlByAffairState(paramMap, listType));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
			buffer.append(getAffairHqlByCondition(paramMap, params));//按affair表字段查询
		}

		if(hasDeduplication) {
			buffer.append(" and affair.id = (");
			buffer.append(" 	select max(affair2.id) from CtpAffair affair2");
			buffer.append("  	where affair2.objectId = summary.id");
			//公文处理人员过滤(包括代理人)
			buffer.append(getHqlByAffairMember(paramMap, memberId, false, "affair2"));
			//公文app及subApp
			buffer.append(getHqlByAffairApp(paramMap, govdocType, listType, false, "affair2"));
			//处理状态
			buffer.append(getHqlByAffairState(paramMap, listType, "affair2"));
			//按节点权限过滤
			buffer.append(getHqlByAffairPolicy(paramMap, params, "affair2"));
			//按条件查询
			if(params.containsKey("condition")) {
				buffer.append(getAffairHqlByCondition(paramMap, params, "affair2"));//按affair表字段查询
			}
			buffer.append(" )");
		}

		if(listType != null){
			if(listType.startsWith("listDone")) {//已办
				buffer.append(" order by affair.completeTime desc");
			} else if(listType.startsWith("listPending")) {//待办
				buffer.append(" order by affair.receiveTime desc");
			} else if(listType.startsWith("listWaitSend")) {//待发
				buffer.append(" order by summary.createTime desc");
			} else {
				buffer.append(" order by summary.createTime desc");
			}
		}

		if(flipInfo != null) {
			flipInfo.setNeedTotal(true);
		}
		List<Object[]> result = super.find(buffer.toString(), -1, -1, paramMap);

		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, hasDeduplication);
				voList.add(vo);
			}
		}

		return voList;
	}

	/**
	 * 发文/收文/签报登记簿数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findRegisterList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();

		String listType = params.get("listType");
		Long orgAccountId = Long.parseLong(params.get("orgAccountId"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		if("listRecRegister".equals(listType)) {
			buffer.append("select " + RecRegisterAllColumnHql);
			buffer.append(" from EdocSummary summary");
			buffer.append(" , GovdocRegister register");
		} else {
			buffer.append("select " + RegisterAllColumnHql);
			buffer.append(" from EdocSummary summary");
		}
		buffer.append(" where summary.orgAccountId = :orgAccountId");
		paramMap.put("orgAccountId", orgAccountId);
		if("listRecRegister".equals(listType)) {
			buffer.append(" and summary.id = register.summaryId");
		}

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文GovdocType
		buffer.append(getHqlByGovdocType(paramMap, listType));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}

		if(listType != null ){
			if(listType.startsWith("listDone")) {//已办
				buffer.append(" order by affair.completeTime desc");
			} else if(listType.startsWith("listPending")) {//待办
				buffer.append(" order by affair.receiveTime desc");
			} else if(listType.startsWith("listWaitSend")) {//待发
				buffer.append(" order by summary.createTime desc");
			} else {
				buffer.append(" order by summary.createTime desc");
			}
		}
		if(flipInfo != null) {
			flipInfo.setNeedTotal(true);
		}
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				int n = vo.toRegister(object);
				vo.toRegisterClob(object, n );
				voList.add(vo);
			}
		}

		return voList;
	}

	/**
	 * 管理员查询收发文的数量
	 * @param params
	 * @return
	 */
	public Integer getRegisterCount(Map<String, String> params){
		int count = 0;

		String listType = params.get("listType");
		Long orgAccountId = Long.parseLong(params.get("orgAccountId"));

		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		if("listRecRegister".equals(listType)) {
			buffer.append("select " + RecRegisterAllColumnHql);
			buffer.append(" from EdocSummary summary");
			buffer.append(" , GovdocRegister register");
		} else {
			buffer.append("select " + RegisterAllColumnHql);
			buffer.append(" from EdocSummary summary");
		}
		buffer.append(" where summary.orgAccountId = :orgAccountId");
		paramMap.put("orgAccountId", orgAccountId);
		if("listRecRegister".equals(listType)) {
			buffer.append(" and summary.id = register.summaryId");
		}

		//公文流程状态
		buffer.append(getHqlBySummaryState(paramMap, listType));
		//公文GovdocType
		buffer.append(getHqlByGovdocType(paramMap, listType));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}

		count = DBAgent.count(buffer.toString(), paramMap);


		return count;
	}

	/**
	 * 公文交换-待发送列表数据数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findSendPendingList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary, CtpAffair affair");
		buffer.append(" where summary.id =  affair.objectId and affair.app = :app and affair.subApp in (:subApp) and affair.state = :affairState ");

		List<Integer> subAppList = new ArrayList<Integer>();
		subAppList.add(ApplicationSubCategoryEnum.edoc_fawen.key());
		subAppList.add(ApplicationSubCategoryEnum.old_exSend.key());
		paramMap.put("app", ApplicationCategoryEnum.edoc.key());
		paramMap.put("subApp", subAppList);
		paramMap.put("affairState", StateEnum.col_pending.key());

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.receiveTime desc");
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, false);
				voList.add(vo);
			}
		}

		return voList;
	}
	/**
	 * 公文交换-待发送列表数据总数查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	public int findSendPendingListCount(Map<String, String> params) throws BusinessException {
		int resultCount = 0;
		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select count(summary.id) from EdocSummary summary, CtpAffair affair");
		buffer.append(" where summary.id =  affair.objectId and affair.app = :app and affair.subApp in (:subApp) and affair.state = :affairState ");

		List<Integer> subAppList = new ArrayList<Integer>();
		subAppList.add(ApplicationSubCategoryEnum.edoc_fawen.key());
		subAppList.add(ApplicationSubCategoryEnum.old_exSend.key());
		paramMap.put("app", ApplicationCategoryEnum.edoc.key());
		paramMap.put("subApp", subAppList);
		paramMap.put("affairState", StateEnum.col_pending.key());

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		//按条件查询
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.receiveTime desc");
		resultCount = DBAgent.count(buffer.toString(), paramMap);
		return resultCount;
	}



	/**
	 * 公文交换-已发送列表数据数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findSendDoneList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		Map<String, Object> paramMap = new HashMap<String, Object>();

		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary, CtpAffair affair");
		buffer.append(" where summary.exchangeSendAffairId = affair.id");

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.completeTime desc");
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, false);
				voList.add(vo);
			}
		}

		return voList;
	}

	/**
	 * 公文交换-待签收列表数据数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public List<GovdocListVO> findSignPendingList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id =  affair.objectId");
		buffer.append(" and summary.exchangeType in (:exchangeType)");
		buffer.append(" and affair.app = :app and affair.subApp in (:subApp) and affair.state = :affairState");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("exchangeType", new ArrayList<Integer>() {
			{
				add(GovdocExchangeTypeEnum.jiaohuan.ordinal());
				add(GovdocExchangeTypeEnum.zhuansw.ordinal());
			}
		});

		List<Integer> subAppList = new ArrayList<Integer>();
		subAppList.add(ApplicationSubCategoryEnum.edoc_jiaohuan.key());
		subAppList.add(ApplicationSubCategoryEnum.old_exSign.key());
		paramMap.put("app", ApplicationCategoryEnum.edoc.key());
		paramMap.put("subApp", subAppList);
		paramMap.put("affairState", StateEnum.col_pending.key());

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.receiveTime desc");
		List<Object[]> result = null;
		if(flipInfo!=null) {
			result = DBAgent.find(buffer.toString(), paramMap, flipInfo);
		}else {
			result = DBAgent.find(buffer.toString(), paramMap);
		}


		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(Strings.isNotEmpty(result)) {
			List<Long> odsummaryIdList = new ArrayList<Long>();
			List<Long> odrecieveIdList = new ArrayList<Long>();
			List<Long> summaryIdList = new ArrayList<Long>();
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, false);
				voList.add(vo);
				if(vo.getGovdocType() != null && vo.getGovdocType().intValue()!=0) {
					summaryIdList.add(vo.getSummaryId());
				} else {
					odsummaryIdList.add(vo.getSummaryId());
					odrecieveIdList.add(vo.getAffairSubObjectId());
				}
			}

			if(Strings.isNotEmpty(summaryIdList)) {
				result = findExchangeSignColumns(summaryIdList);
				if(Strings.isNotEmpty(result)) {
					for(Object[] object : result) {
						for(GovdocListVO vo : voList) {
							Long summaryId = ((Long)object[0]).longValue();
							if(vo.getSummaryId().longValue() == summaryId) {
								vo.toExObject(object);
							}
						}
					}
				}
			}

			if(Strings.isNotEmpty(odsummaryIdList)) {
				result = findOldExchangeSignColumns(odsummaryIdList, odrecieveIdList);
				if(Strings.isNotEmpty(result)) {
					for(Object[] object : result) {
						for(GovdocListVO vo : voList) {
							Long summaryId = ((Long)object[0]).longValue();
							if(vo.getSummaryId().longValue() == summaryId) {
								vo.toOldExObject(object);
							}
						}
					}
				}
			}
		}

		return voList;
	}
	/**
	 * 公文交换-待签收列表数据总条数
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "serial" })
	public int findSignPendingListCount(Map<String, String> params) throws BusinessException {
		int resultCount = 0;
		StringBuilder buffer = new StringBuilder();
		buffer.append("select count(summary.id) from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id =  affair.objectId");
		buffer.append(" and summary.exchangeType in (:exchangeType)");
		buffer.append(" and affair.app = :app and affair.subApp in (:subApp) and affair.state = :affairState");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("exchangeType", new ArrayList<Integer>() {
			{
				add(GovdocExchangeTypeEnum.jiaohuan.ordinal());
				add(GovdocExchangeTypeEnum.zhuansw.ordinal());
			}
		});

		List<Integer> subAppList = new ArrayList<Integer>();
		subAppList.add(ApplicationSubCategoryEnum.edoc_jiaohuan.key());
		subAppList.add(ApplicationSubCategoryEnum.old_exSign.key());
		paramMap.put("app", ApplicationCategoryEnum.edoc.key());
		paramMap.put("subApp", subAppList);
		paramMap.put("affairState", StateEnum.col_pending.key());

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params));
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.receiveTime desc");

		resultCount = DBAgent.count(buffer.toString(), paramMap);
		return resultCount;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findExchangeSignColumns(List<Long> summaryIdList) {
		String hql = "select detail.summaryId, detail.sendAccountId, detail.sendUnit, detail.recUserName, detail.recOrgName, detail.recTime, detail.createTime from GovdocExchangeDetail detail where detail.summaryId in (:summaryId)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryIdList);
		return DBAgent.find(hql, paramMap);
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> findOldExchangeSignColumns(List<Long> summaryIdList, List<Long> odrecieveIdList) {
		String hql = "select detail.edocId, detail.sendUnit,detail.sendTo, detail.recUserId, detail.recTime from EdocRecieveRecord detail where detail.edocId in (:summaryId) and id in (:recieveId)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("summaryId", summaryIdList);
		paramMap.put("recieveId", odrecieveIdList);
		return DBAgent.find(hql, paramMap);
	}

	/**
	 * 公文交换-已签收列表数据数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings({ "unchecked", "serial" })
	public List<GovdocListVO> findSignDoneList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		StringBuilder buffer = new StringBuilder();
		buffer.append("select " + ListAllColumnHql);
		buffer.append(" from EdocSummary summary");
		buffer.append(" , CtpAffair affair");
		buffer.append(" where summary.id =  affair.objectId");
		buffer.append(" and summary.exchangeType in (:exchangeType)");
		buffer.append(" and affair.app = :app and affair.subApp in (:subApp) and affair.state = :affairState");

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("exchangeType", new ArrayList<Integer>() {
			{
				add(GovdocExchangeTypeEnum.jiaohuan.ordinal());
				add(GovdocExchangeTypeEnum.zhuansw.ordinal());
			}
		});

		List<Integer> subAppList = new ArrayList<Integer>();
		subAppList.add(ApplicationSubCategoryEnum.edoc_jiaohuan.key());
		subAppList.add(ApplicationSubCategoryEnum.old_exSign.key());
		paramMap.put("app", ApplicationCategoryEnum.edoc.key());
		paramMap.put("subApp", subAppList);
		paramMap.put("affairState", StateEnum.col_done.key());

		//公文处理人员过滤(包括代理人)
		buffer.append(getHqlByAffairMember(paramMap, Long.valueOf(params.get("memberId")), true));
		//按节点权限过滤
		buffer.append(getHqlByAffairPolicy(paramMap, params, "affair"));

		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
		}
		buffer.append(" order by affair.completeTime desc");
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(Strings.isNotEmpty(result)) {
			List<Long> odsummaryIdList = new ArrayList<Long>();
			List<Long> odrecieveIdList = new ArrayList<Long>();
			List<Long> summaryIdList = new ArrayList<Long>();
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toObject(object, false);
				voList.add(vo);
				if(vo.getGovdocType() != null && vo.getGovdocType().intValue()!=0) {
					summaryIdList.add(vo.getSummaryId());
				} else {
					odsummaryIdList.add(vo.getSummaryId());
					odrecieveIdList.add(vo.getAffairSubObjectId());
				}
			}

			if(Strings.isNotEmpty(summaryIdList)) {
				result = findExchangeSignColumns(summaryIdList);
				if(Strings.isNotEmpty(result)) {
					for(Object[] object : result) {
						for(GovdocListVO vo : voList) {
							Long summaryId = ((Long)object[0]).longValue();
							if(vo.getSummaryId().longValue() == summaryId) {
								vo.toExObject(object);
							}
						}
					}
				}
			}

			if(Strings.isNotEmpty(odsummaryIdList)) {
				result = findOldExchangeSignColumns(odsummaryIdList, odrecieveIdList);
				if(Strings.isNotEmpty(result)) {
					for(Object[] object : result) {
						for(GovdocListVO vo : voList) {
							Long summaryId = ((Long)object[0]).longValue();
							if(vo.getSummaryId().longValue() == summaryId) {
								vo.toOldExObject(object);
							}
						}
					}
				}
			}
		}

		return voList;
	}

	/**
	 * 公文交换-已回退列表数据数据展示及查询
	 * @param flipInfo
	 * @param params
	 * @return
	 * @throws BusinessException
	 */
	@SuppressWarnings("unchecked")
	public List<GovdocListVO> findFallbackList(FlipInfo flipInfo, Map<String, String> params) throws BusinessException {
		StringBuilder buffer = new StringBuilder();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		buffer.append("select " + BackExchangeHql);
		buffer.append(" from EdocSummary summary,GovdocExchangeDetailLog log,GovdocExchangeMain main,GovdocExchangeDetail detail");
		buffer.append(" where summary.id = log.backSummaryId");
		buffer.append(" and log.detailId = detail.id ");
		buffer.append(" and detail.mainId = main.id");
		buffer.append(" and summary.transferStatus = :transferStatus");
		paramMap.put("transferStatus", TransferStatus.stepbackWaitSend.getKey());
		buffer.append(" and detail.recAccountId = :recAccountId ");
		buffer.append(" and main.type <> :type ");
		paramMap.put("type", 1);//不等于联合发文
		paramMap.put("recAccountId", Long.valueOf(params.get("orgAccountId")));
		if(params.containsKey("condition")) {
			buffer.append(getHqlByCondition(paramMap, params));//按summary表字段查询
			if(Strings.isNotBlank(params.get("exchangeSendUnitId")) || Strings.isNotBlank(params.get("exchangeSendUnitName"))) {//按发文单位查询(无Affair关联)
				buffer.append(getExchangeHqlBySendUnitId(paramMap, params));
			}
		}

		buffer.append(" order by summary.completeTime desc");
		List<Object[]> result = DBAgent.find(buffer.toString(), paramMap, flipInfo);

		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(Strings.isNotEmpty(result)) {
			for(Object[] object : result) {
				GovdocListVO vo = new GovdocListVO();
				vo.toExchangeFallback(object);
				voList.add(vo);
			}
		}

		return voList;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<WorkflowData> getAdminWfDataList(FlipInfo flipInfo,Map<String,Object> conditionParam,long accountId,boolean isPage,FlipInfo fi) {
		Map<String,Object> paramMap = new HashMap<String, Object>();
		String operationTypeIdsStr =(String) conditionParam.get("operationTypeIds");
        String operationType = (String) conditionParam.get("operationType");
        int edocType = (Integer)conditionParam.get("edocType");
        String[] operationTypeIds = null;
        if(operationTypeIdsStr!=null&&!"".equals(operationTypeIdsStr.trim())){
            operationTypeIds = operationTypeIdsStr.split(",");
        }
        if(operationType!= null){
            if("".equals(operationType.trim()))
                operationType= null;
        }
        int flowstate = 0;
        String flowstateStr = (String)conditionParam.get("flowstate");
        if(Strings.isNotBlank(flowstateStr)){
            flowstate = Integer.parseInt(flowstateStr);
        }
	    StringBuilder buffer = new StringBuilder();
	    buffer.append("select " + AdminManagerHql);
	    buffer.append(" from CtpAffair affair,EdocSummary summary,OrgUnit u");
	    if("true".equals(conditionParam.get("onlyObjectId"))){
	    	buffer.append(" where u.id = summary.orgDepartmentId and summary.id = affair.objectId and affair.objectId in (:objctIds)");
	    	paramMap.put("objctIds", conditionParam.get("objectIds"));
	    }else{
	    	boolean needTemplate = EdocWorkflowManageHandler.TEMPLETE_WORKFLOW.equals(operationType)&& (operationTypeIds == null|| operationTypeIds.length==0);
	    	if(needTemplate) {
	    		buffer.append(",CtpTemplate t ");
	    	}

	    	buffer.append(" where u.id = summary.orgDepartmentId and summary.id = affair.objectId ");
	    	//buffer.append(" and summary.edocType = :edocType");
	    	if(needTemplate) {
	    		buffer.append(" and t.id = summary.templeteId and t.system = :isSystem");
	    		paramMap.put("isSystem", true);
	    	}
	    	if(null != conditionParam.get("needOldEdoc") && "false".equals(conditionParam.get("needOldEdoc"))){
	    		buffer.append(" and summary.govdocType != 4 ");
	    	}
	    	buffer.append(" and summary.state = :summarystate");
	    	paramMap.put("summarystate", flowstate);

	    	buffer.append(" and summary.isQuickSend = :isQuickSend");
	    	paramMap.put("isQuickSend", Boolean.FALSE);

	    	buffer.append(" and affair.app = :app");
	    	paramMap.put("app", ApplicationCategoryEnum.edoc.key());
	    	//OA-178994 caseid为空的不用查询出来 和协同保持一致
	    	buffer.append(" and summary.caseId is not null");

	    	buffer.append(" and affair.subApp in (:subApp)");
	    	List<Integer> subAppList = new ArrayList<Integer>();
	    	if (edocType==0) {
	    		subAppList.add(ApplicationSubCategoryEnum.edoc_fawen.key());
	    		subAppList.add(ApplicationSubCategoryEnum.old_edocSend.key());
	    	} else if(edocType==2) {//签报
	    		subAppList.add(ApplicationSubCategoryEnum.edoc_qianbao.key());
	    		subAppList.add(ApplicationSubCategoryEnum.old_edocSign.key());
	    	}else {
	    		subAppList.add(ApplicationSubCategoryEnum.edoc_shouwen.key());
	    		subAppList.add(ApplicationSubCategoryEnum.edoc_jiaohuan.key());
	    		subAppList.add(ApplicationSubCategoryEnum.old_edocRec.key());
	    	}
	    	paramMap.put("subApp", subAppList);

	    	buffer.append(" and (affair.state = :sentstate or (affair.state = :waitstate and affair.subState = :waitsubstate))");
	    	paramMap.put("sentstate", StateEnum.col_sent.key());
	    	paramMap.put("waitstate", StateEnum.col_waitSend.key());
	    	paramMap.put("waitsubstate", SubStateEnum.col_pending_specialBacked.key());
	    	Map<String,List<Long>> senderMap = (Map<String,List<Long>>)conditionParam.get("senderMap");

	    	String senders = (String) conditionParam.get("senders");
	    	if(Strings.isNotBlank(senders)) {
	    		List<V3xOrgMember> memberList = GovdocOrgHelper.getMembersByTypeAndIds(senders);
	    		if(Strings.isNotEmpty(memberList)) {
	    			if(memberList.size() > 1000) {
	    				memberList =  memberList.subList(0,1000);
	    			}
	    			List<Long> memberIdList = new ArrayList<Long>();
	    			for(V3xOrgMember mem : memberList) {
	    				memberIdList.add(mem.getId());
	    			}
	    			buffer.append(" and summary.startUserId in (:sendMember) ");
	    			paramMap.put("sendMember", memberIdList);
	    		}
	    	}

	    	//单位类型， 查的自由流程或者 模板流程
	    	if(Strings.isNotBlank(operationType)){
	    		//如果发起对象没有选择，然而是查整个单位
	    		if(senderMap == null || senderMap.size() == 0) {
	    			buffer.append(" and summary.orgAccountId = :accountId ");
	    			paramMap.put("accountId",  accountId);
	    		}
	    		//自由流程
	    		if(EdocWorkflowManageHandler.COMMON_WORKFLOW.equals(operationType)){
	    			buffer.append(" and summary.templeteId is null ");
	    		} else { //模板流程
	    			buffer.append(" and summary.templeteId is not null ");
	    			if(operationTypeIds != null && operationTypeIds.length > 0){
	    				List<Long> templeteList = new ArrayList<Long>();
	    				for(String tid : operationTypeIds){
	    					templeteList.add(Long.parseLong(tid));
	    				}
	    				Map<String, Object> queryOldIdsParam = new HashMap<String, Object>();
	    				queryOldIdsParam.put("newTids", templeteList);
	    				List<Long> oldDataIds = DBAgent.find(queryOldDataIdByNewDataId, queryOldIdsParam);
	    				buffer.append(" and summary.templeteId in (:tid) ");
	    				templeteList.addAll(oldDataIds);
	    				paramMap.put("tid", templeteList);
	    			}
	    		}
	    	}
	    	String subject = (String) conditionParam.get("subject");
	    	if(Strings.isNotBlank(subject)) {
	    		buffer.append(" and summary.subject like :subject");
	    		paramMap.put("subject", "%" + SQLWildcardUtil.escape(subject.trim()) + "%");
	    	}
	    	Date beginDate = (Date)conditionParam.get("bDate");
	    	Date endDate = (Date)conditionParam.get("eDate");
	    	if(beginDate != null) {
	    		buffer.append(" and summary.createTime >= :beginTime");
	    		paramMap.put("beginTime", beginDate);
	    	}
	    	if(endDate != null) {
	    		buffer.append(" and summary.createTime <= :endTime");
	    		paramMap.put("endTime", endDate);
	    	}

	    	String deadlineBeginDateStr = (String) conditionParam.get("deadlineBeginDate");
	    	if(Strings.isNotEmpty(deadlineBeginDateStr)) {
	    		Date deadlineBeginDate = Datetimes.getTodayFirstTime(deadlineBeginDateStr);
	    		buffer.append(" and summary.deadlineDatetime >= :deadlineBeginDate");
	    		paramMap.put("deadlineBeginDate", deadlineBeginDate);
	    	}
	    	String deadlineEndDateStr = (String) conditionParam.get("deadlineEndDate");
	    	if(Strings.isNotEmpty(deadlineEndDateStr)) {
	    		Date deadlineEndDate = Datetimes.getTodayFirstTime(deadlineEndDateStr);
	    		buffer.append(" and summary.deadlineDatetime >= :deadlineEndDate");
	    		paramMap.put("deadlineEndDate", deadlineEndDate);
	    	}

	    	buffer.append(" order by summary.createTime desc ");
	    }

        List<WorkflowData> flowList = new ArrayList<WorkflowData>();

        List list = null;
        if(flipInfo != null){
        	list = DBAgent.find(buffer.toString(), paramMap, flipInfo);
        } else {
        	if(isPage) {
        		list = DBAgent.find(buffer.toString(), paramMap, fi);
	        } else {
	        	list = DBAgent.find(buffer.toString(), paramMap);
	        }
        }

        for(int i=0;i<list.size();i++) {
            Object[] obj = (Object[])list.get(i);
            WorkflowData flow = new WorkflowData();
            int j=0;
            int sub_app = -1;
            Object sub_app_obj = obj[j++];
            if(null != sub_app_obj){
            	sub_app =(Integer)sub_app_obj;
            }
            flow.setEdocType(-1);
            flow.setSummaryId(String.valueOf(obj[j++]));
            //应用类型
            int app = Integer.parseInt(String.valueOf(obj[j++]));
            flow.setAppEnum(app);
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

            Long startUserId = (Long)(obj[j++]);
            if(startUserId != null) {
                V3xOrgMember member = null;
                try {
                    member = orgManager.getMemberById(startUserId);
                } catch (BusinessException e) {
                }
                if(member != null) {
                    flow.setInitiator(member.getName());
                }
            }
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
            if(app == ApplicationCategoryEnum.edoc.key() && sub_app != -1){
            	if(sub_app == ApplicationSubCategoryEnum.edoc_fawen.key()){//發文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.send");
            		flow.setAppEnumStr(ApplicationCategoryEnum.govdocSend.name());
            		flow.setEdocType(FormType.govDocSendForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.govdocSend.name());
            	}else if(sub_app == ApplicationSubCategoryEnum.edoc_shouwen.key()){//收文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            		flow.setAppEnumStr(ApplicationCategoryEnum.govdocRec.name());
            		flow.setEdocType(FormType.govDocReceiveForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.govdocRec.name());
            	}else if(sub_app == ApplicationSubCategoryEnum.edoc_qianbao.key()){//签报
            		appStr = ResourceUtil.getString("edoc.docmark.inner.signandreport");
            		flow.setAppEnumStr(ApplicationCategoryEnum.govdocSign.name());
            		flow.setEdocType(FormType.govDocSignForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.govdocSign.name());
            	}else if(sub_app == ApplicationCategoryEnum.edocSend.getKey()){//發文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.send");
            		flow.setAppEnumStr(ApplicationCategoryEnum.edocSend.name());
            		flow.setEdocType(FormType.govDocSendForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.edocSend.name());
            	}else if(sub_app == ApplicationCategoryEnum.edocRec.getKey()){//收文
            		appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            		flow.setAppEnumStr(ApplicationCategoryEnum.edocRec.name());
            		flow.setEdocType(FormType.govDocReceiveForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.edocRec.name());
            	}else if(sub_app == ApplicationCategoryEnum.edocSign.getKey()){//签报
            		appStr = ResourceUtil.getString("edoc.docmark.inner.signandreport");
            		flow.setAppEnumStr(ApplicationCategoryEnum.edocSign.name());
            		flow.setEdocType(FormType.govDocSignForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.edocSign.name());
            	}else{
            		appStr = ResourceUtil.getString("edoc.docmark.inner.receive");
            		flow.setAppEnumStr(ApplicationCategoryEnum.govdocRec.name());
            		flow.setEdocType(FormType.govDocExchangeForm.getKey());
            		flow.setNps(ApplicationCategoryEnum.govdocSend.name());
            	}
            }
            flow.setAppType(appStr);
            flow.setAppEnumStr("edoc");
            EdocSummary summary = new EdocSummary();
            String completeTime = String.valueOf(obj[j++]);
            if(Strings.isNotBlank(completeTime)&& !"null".equals(completeTime)){
            	summary.setCompleteTime(Timestamp.valueOf(completeTime));
            }
            Date deadlineDatetime=(Date)obj[j++];
            //设置流程期限显示内容
            flow.setDeadlineDatetimeName(WFComponentUtil.getDeadLineName(deadlineDatetime));
            String currentNodesInfo = (String)(obj[j++]);
            if(flowstate == flowState.terminate.ordinal() || flowstate == flowState.finish.ordinal()){
    			currentNodesInfo = "";
    		}else if(Strings.isNotBlank(currentNodesInfo) && currentNodesInfo.indexOf(";;") != -1){
            	currentNodesInfo = currentNodesInfo.replaceAll(";;", ";");
            }
            summary.setCurrentNodesInfo(currentNodesInfo);
            flow.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo(summary));
            flow.setSecretLevel(String.valueOf((obj[j++])));

            Object formAppIdObj = obj[j++];
            flow.setFormAppId(formAppIdObj == null ? null : ((Number)formAppIdObj).longValue());

            Object formRecordIdObj = obj[j++];
            flow.setFormRecordId(formRecordIdObj == null ? null : ((Number)formRecordIdObj).longValue());
            if(flowstate == 1 || flowstate == 3){
               	flow.setEndFlag(0); //0 - 结束
            }
            flowList.add(flow);
        }
	    return flowList;
	}


	/************************ 以下为公文列表辅助Hql方法 start ***************************/
	private String getHqlBySummaryState(Map<String, Object> paramMap, String listType) {
		StringBuilder buffer = new StringBuilder();
		//公文流程状态
		if(listType != null) {
	        String flowState = GovdocListTypeEnum.getFlowStateName(listType);
			if(Strings.isNotBlank(flowState) && !"-1".equals(flowState)) {
				List<Integer> flowStateList = new ArrayList<Integer>();
				String[] flowStates = flowState.split(",");
				if(flowStates != null) {
					for(int i=0; i<flowStates.length; i++) {
						flowStateList.add(Integer.parseInt(flowStates[i]));
					}
				}
				if(flowStateList.size() == 1) {
					buffer.append(" and summary.state = :summaryState");
					paramMap.put("summaryState", flowStateList.get(0));
				} else {
					buffer.append(" and summary.state in (:summaryState)");
					paramMap.put("summaryState", flowStateList);
				}
			}

			if (GovdocListTypeEnum.listSent.getKey().equals(listType)) {
				buffer.append(" and (summary.exchangeType is null or summary.exchangeType <> :exchangeType)");
				paramMap.put("exchangeType", 1);
			}
		} else {
			List<Integer> flowStateList = new ArrayList<Integer>();
			flowStateList.add(EdocConstant.flowState.run.ordinal());
			flowStateList.add(EdocConstant.flowState.finish.ordinal());
			flowStateList.add(EdocConstant.flowState.terminate.ordinal());
			buffer.append(" and summary.state in (:summaryState)");
			paramMap.put("summaryState", flowStateList);
		}
		return buffer.toString();
	}
	private String getHqlBySummaryTemplateIds(Map<String, Object> paramMap, Map<String, String> params) {
		StringBuilder buffer = new StringBuilder();
		//公文流程状态
        String templateIds = params.get("templateIds") == null ? null : (String)params.get("templateIds");
		if(Strings.isNotBlank(templateIds)) {
			List<Long> templateIdList = new ArrayList<Long>();
			String[] templateIdArr = templateIds != null ? templateIds.split(",") : null;
			if(templateIdArr != null) {
				for(int i=0; i<templateIdArr.length; i++) {
					templateIdList.add(Long.parseLong(templateIdArr[i]));
				}
				if(templateIdList.size() == 1) {
					buffer.append(" and summary.templeteId = :templeteId");
					paramMap.put("templeteId", templateIdList.get(0));
				} else {
					buffer.append(" and summary.templeteId in (:templeteId)");
					paramMap.put("templeteId", templateIdList);
				}
			}
		}
		return buffer.toString();
	}
	private String getHqlByGovdocType(Map<String, Object> paramMap, String listType) {
		StringBuilder buffer = new StringBuilder();
		if(!"listRecRegister".equals(listType)) {//发文/签报登记簿
			buffer.append(" and (");
			buffer.append(" 	summary.govdocType in (:govdocType) ");
			buffer.append(" 	or (summary.govdocType = :oldgovdocType and summary.edocType in (:edocType))");
			buffer.append(" )");

			paramMap.put("oldgovdocType", ApplicationSubCategoryEnum.old_summary.getKey());
			paramMap.put("edocType", OldEdocTypeEnum.rec.ordinal());
	        //签报数据
			if("listSignRegister".equals(listType)) {
				paramMap.put("govdocType", ApplicationSubCategoryEnum.edoc_qianbao.getKey());
				paramMap.put("edocType", OldEdocTypeEnum.sign.ordinal());
			} else {
				paramMap.put("edocType", OldEdocTypeEnum.send.ordinal());
				paramMap.put("govdocType", ApplicationSubCategoryEnum.edoc_fawen.getKey());
			}
        }
		return buffer.toString();
	}

	private String getHqlByAffairApp(Map<String, Object> paramMap, String govdocType, String listType, boolean hasExData) {
		return getHqlByAffairApp(paramMap, govdocType, listType, hasExData, "affair");
	}
	private String getHqlByAffairApp(Map<String, Object> paramMap, String govdocType, String listType, boolean hasExData, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		//公文处理状态
		List<Integer> subAppList = new ArrayList<Integer>();
		if(Strings.isNotBlank(govdocType)) {
			String[] govdocTypes = govdocType.split(",");
			for(String type : govdocTypes) {
				Integer subApp = Integer.parseInt(type);
				if(!subAppList.contains(subApp)) {
					subAppList.add(subApp);
				}
				if(subApp.intValue() == ApplicationSubCategoryEnum.edoc_fawen.key()) {//发文管理
					subAppList.add(ApplicationSubCategoryEnum.old_edocSend.key());
					if(hasExData) {
						subAppList.add(ApplicationSubCategoryEnum.old_exSend.key());
					}
				} else if(subApp.intValue() == ApplicationSubCategoryEnum.edoc_shouwen.key()) {//收文管理
					subAppList.add(ApplicationSubCategoryEnum.old_edocRec.key());
				} else if(subApp.intValue() == ApplicationSubCategoryEnum.edoc_qianbao.key()) {//签报管理
					subAppList.add(ApplicationSubCategoryEnum.old_edocSign.key());
				} else if(subApp.intValue() == ApplicationSubCategoryEnum.edoc_jiaohuan.key()) {//交换列表
					if(hasExData) {
						if(listType.startsWith("listSent") || listType.startsWith("listWaitSend")) {//收文已发没有交换数据，因为状态数据的state为4，数据到已办中去了
							continue;
						}
						subAppList.add(ApplicationSubCategoryEnum.old_exSign.key());
						subAppList.add(ApplicationSubCategoryEnum.old_edocRegister.key());
						subAppList.add(ApplicationSubCategoryEnum.old_edocRecDistribute.key());
					}
				}
			}
		} else {//公文待办/已办/待发/已发列表
			if(listType.startsWith("listSent") || listType.startsWith("listWaitSend")) {//公文待发/已发列表不展示交换已发/待发数据
				subAppList.add(ApplicationSubCategoryEnum.edoc_fawen.key());
				subAppList.add(ApplicationSubCategoryEnum.edoc_shouwen.key());
				subAppList.add(ApplicationSubCategoryEnum.edoc_qianbao.key());
				subAppList.add(ApplicationSubCategoryEnum.old_edocSend.key());
				subAppList.add(ApplicationSubCategoryEnum.old_edocRec.key());
				subAppList.add(ApplicationSubCategoryEnum.old_edocSign.key());
			}
		}

		//新老公文统一app为4
		buffer.append(" and "+affairTable+".app = :affariApp");
		paramMap.put("affariApp", ApplicationCategoryEnum.edoc.key());

		//新老公文区分subApp
		if(Strings.isNotEmpty(subAppList)) {
			if(subAppList.size() == 1) {
				buffer.append(" and "+affairTable+".subApp = :affairSubApp");
				paramMap.put("affairSubApp", subAppList.get(0));
			} else {
				buffer.append(" and "+affairTable+".subApp in (:affairSubApp)");
				paramMap.put("affairSubApp", subAppList);
			}
		}
		return buffer.toString();
	}

	private String getHqlByAffairMember(Map<String, Object> paramMap, Long memberId, boolean hasAgent) {
		return getHqlByAffairMember(paramMap, memberId, hasAgent, "affair");
	}

	private String getHqlByAffairMember(Map<String, Object> paramMap, Long memberId, boolean hasAgent, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" and " + affairTable + ".delete = :isDelete");
		paramMap.put("isDelete", Boolean.FALSE);

		buffer.append(" and (" + affairTable + ".memberId=:memberId");
		paramMap.put("memberId", memberId);
		if (hasAgent) {
			// 以当前登录用户为判断标准，首先判断当前用户是否为代理人(即当前用户替别人干活)，如果当前登录用户为代理人，则用!affair.getMemberId().equals(userId)来判断设值
			// 如果当前登录用户不是代理人，则判断其是否是被代理人，如果是被代理人，则判断其设置的代理人是否有效(即：affair事项处于代理期限内)
			// 查询我所代理的人(我为代理人，我给别人干活)列表
			// 以前的方式有性能问题，查询方式和协同保持一致 OA-184924
			buffer.append(" or " + affairTable + ".proxyMemberId=:proxyMemberId");
			paramMap.put("proxyMemberId", memberId);
		}
		buffer.append(")");
		return buffer.toString();
	}

	private String getHqlByAffairState(Map<String, Object> paramMap, String listType) {
		return getHqlByAffairState(paramMap, listType, "affair");
	}
	@SuppressWarnings("unchecked")
	private String getHqlByAffairState(Map<String, Object> paramMap, String listType, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		String affairState = GovdocListTypeEnum.getStateName(listType);
		if(Strings.isNotBlank(affairState) && !"-1".equals(affairState)) {
			List<Integer> stateList = new ArrayList<Integer>();//公文处理状态
			String[] states = affairState.split(",");
			if(states != null) {
				for(int i=0; i<states.length; i++) {
					stateList.add(Integer.parseInt(states[i]));
				}
			}

			List<Integer> affairSubAppList = new ArrayList<Integer>();
			Object affairSubApp = paramMap.get("affairSubApp");
			if(affairSubApp != null) {
				if(affairSubApp instanceof List) {
					affairSubAppList.addAll((List<Integer>)affairSubApp);
				} else {
					affairSubAppList.add((Integer)affairSubApp);
				}
			}

			buffer.append(" and (");
			if(stateList.contains(2) && affairSubAppList.contains(4)) {//已发&交换
				Integer sent = StateEnum.col_sent.key();
				Integer exchange = ApplicationSubCategoryEnum.edoc_jiaohuan.key();
				stateList.remove(sent);
				affairSubAppList.remove(exchange);

				buffer.append("	    ("+affairTable+".state = :stateSent");
				paramMap.put("stateSent", sent);

				if(Strings.isNotEmpty(affairSubAppList)) {
					if(affairSubAppList.size() == 1) {
						buffer.append("  and "+affairTable+".subApp = :subAppSent");
						paramMap.put("subAppSent", affairSubAppList.get(0));
					} else {
						buffer.append("  and "+affairTable+".subApp in (:subAppSent)");
						paramMap.put("subAppSent", affairSubAppList);
					}
				}
				buffer.append("	    )");//已发end

				if(Strings.isNotEmpty(stateList)) {
					buffer.append("	or");
				}
			}//已发&交换 end

			if(Strings.isNotEmpty(stateList)) {
				if(stateList.size() == 1) {
					buffer.append(" " +affairTable+".state = :affairState");
					paramMap.put("affairState", stateList.get(0));
				} else {
					buffer.append(" "+affairTable+".state in (:affairState)");
					paramMap.put("affairState", stateList);
				}
			}
			buffer.append(" )");//and end

		}

		//公文处理子状态
		List<Integer> substateList = new ArrayList<Integer>();
		String substate = GovdocListTypeEnum.getSubStateName(listType);
		if(Strings.isNotBlank(substate) && !"-1".equals(substate)) {
			String[] substates = substate.split(",");
			if(substates != null) {
				for(int i=0; i<substates.length; i++) {
					substateList.add(Integer.parseInt(substates[i]));
				}
			}
			if(Strings.isNotEmpty(substateList)) {
				if(substateList.size() == 1) {
					buffer.append(" and "+affairTable+".subState = :affairSubState");
					paramMap.put("affairSubState", substateList.get(0));
				} else {
					buffer.append(" and "+affairTable+".subState in (:affairSubState)");
					paramMap.put("affairSubState", substateList);
				}
			}
		}
		//已指定回退数据不在待办列表中查询--同步协同查询
		if(listType.contains("listPending") || listType.contains("listReading")){
			buffer.append(" and "+affairTable+".subState not in (:specialBackSubState)");
			List<Integer> specialBackSubState = new ArrayList<Integer>();
			specialBackSubState.add(SubStateEnum.col_pending_specialBack.getKey());
			specialBackSubState.add(SubStateEnum.col_pending_specialBackCenter.getKey());
			paramMap.put("specialBackSubState",specialBackSubState);
		}

		return buffer.toString();
	}

	private String getHqlByAffairPolicy(Map<String, Object> paramMap, Map<String, String> params) {
		return getHqlByAffairPolicy(paramMap, params, "affair");
	}
	private String getHqlByAffairPolicy(Map<String, Object> paramMap, Map<String, String> params, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		if(Strings.isNotBlank(params.get("nodePolicy"))) {
			List<String> nodePolicyList = new ArrayList<String>();
			for(String nodePolicy : params.get("nodePolicy").split(",")) {
				nodePolicyList.add(nodePolicy);
			}
			if (nodePolicyList.size() == 1) {
				buffer.append(" and "+affairTable+".nodePolicy = :nodePolicy");
				paramMap.put("nodePolicy", nodePolicyList.get(0));
			}else{
				buffer.append(" and "+affairTable+".nodePolicy in (:nodePolicy)");
				paramMap.put("nodePolicy", nodePolicyList);
			}
		}
		return buffer.toString();
	}

	private String getHqlByAffairNotInPolicy(Map<String, Object> paramMap, Map<String, String> params) {
		return getHqlByAffairNotInPolicy(paramMap, params, "affair");
	}
	private String getHqlByAffairNotInPolicy(Map<String, Object> paramMap, Map<String, String> params, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		if(Strings.isNotBlank(params.get("notInNodePolicy"))) {
			buffer.append(" and "+affairTable+".nodePolicy not in (:notInNodePolicy)");
			List<String> notInNodePolicyList = new ArrayList<String>();
			for(String notInNodePolicy : params.get("notInNodePolicy").split(",")) {
				notInNodePolicyList.add(notInNodePolicy);
			}
			paramMap.put("notInNodePolicy", notInNodePolicyList);
		}
		return buffer.toString();
	}

	/**
	 * 按summary表字段查询
	 * @param paramMap  hql的参数
	 * @param params  传递进来的条件
	 * @return
	 */
//	zhou
	private String getHqlByCondition(Map<String, Object> paramMap, Map<String, String> params) {
		StringBuilder buffer = new StringBuilder();
		if(params.containsKey("condition")) {
			//处理领导批示编号查询
        	if (Strings.isNotBlank(params.get("pishiName")) || Strings.isNotBlank(params.get("pishiYear")) || Strings.isNotBlank(params.get("pishiNo"))) {
        		List<Long> summaryIdList = getSummaryIdByPishi(params);
        		if(Strings.isNotEmpty(summaryIdList)) {
        			if(summaryIdList.size() < 1000) {
        				buffer.append(" and summary.id in (:summaryIds)");
        				paramMap.put("summaryIds", summaryIdList);
        			} else {//分页，oracle不支持in中超过1000

        			}
        		}else{
        			buffer.append(" and 1=0");
        		}
        	}
			if(Strings.isNotBlank(params.get("subject"))) {//按标题查询
				buffer.append(" and summary.subject like :subject");
				paramMap.put("subject", "%" + GovdocUtil.convertSpecialChat(params.get("subject")) + "%");
			}
			if(Strings.isNotBlank(params.get("startUserId"))) {//按发起人查询(无Affair关联)
				buffer.append(getHqlByStartMemberId(paramMap, params));
			}
			if(Strings.isNotBlank(params.get("docMark"))) {//按公文文号查询
				buffer.append(" and summary.docMark like :docMark");
				paramMap.put("docMark", "%" + GovdocUtil.convertSpecialChat(params.get("docMark")) + "%");
			}
			if(Strings.isNotBlank(params.get("serialNo"))) {//按内部文号查询
				buffer.append(" and summary.serialNo like :serialNo");
				paramMap.put("serialNo", "%" + GovdocUtil.convertSpecialChat(params.get("serialNo")) + "%");
			}
//			zhou
			if(Strings.isNotBlank(params.get("sendUnit"))) {//按内部文号查询
				buffer.append(" and summary.sendUnit like :sendUnit");
				paramMap.put("sendUnit", "%" + GovdocUtil.convertSpecialChat(params.get("sendUnit")) + "%");
			}
			if(Strings.isNotBlank(params.get("hasArchive"))){//按是否归档查询
				buffer.append(" and summary.hasArchive = :hasArchive");
				String hasArchive = params.get("hasArchive").toString();
				if("1".equals(hasArchive)){
					paramMap.put("hasArchive", true);
				}else if("0".equals(hasArchive)){
					paramMap.put("hasArchive", false);
				}
			}
			if(Strings.isNotBlank(params.get("signMark"))) {//按内部文号查询
				buffer.append(" and summary.docMark2 like :signMark");
				paramMap.put("signMark", "%" + GovdocUtil.convertSpecialChat(params.get("signMark")) + "%");
			}
			if(Strings.isNotBlank(params.get("phone"))) {//按联系电话查询
				buffer.append(" and summary.phone like :phone");
				paramMap.put("phone", "%" + GovdocUtil.convertSpecialChat(params.get("phone")) + "%");
			}
			if(Strings.isNotBlank(params.get("sendUnit"))) {//按发文单位查询
				buffer.append(" and summary.sendUnit like :sendUnit");
				paramMap.put("sendUnit", "%" + GovdocUtil.convertSpecialChat(params.get("sendUnit")) + "%");
			}
			if(Strings.isNotBlank(params.get("sendDepartment"))) {//按发文部门查询
				buffer.append(" and summary.sendDepartment like :sendDepartment");
				paramMap.put("sendDepartment", "%" + GovdocUtil.convertSpecialChat(params.get("sendDepartment")) + "%");
			}
			if(Strings.isNotBlank(params.get("sendTo"))) {//按主送单位查询
				buffer.append(" and summary.sendTo like :sendTo");
				paramMap.put("sendTo", "%" + GovdocUtil.convertSpecialChat(params.get("sendTo")) + "%");
			}
			if(Strings.isNotBlank(params.get("createPerson"))) {//按拟稿人查询
				buffer.append(" and summary.createPerson like :createPerson");
				paramMap.put("createPerson", "%" + GovdocUtil.convertSpecialChat(params.get("createPerson")) + "%");
			}
			if(Strings.isNotBlank(params.get("auditor"))) {//按审核人查询
				buffer.append(" and summary.auditor like :auditor");
				paramMap.put("auditor", "%" + GovdocUtil.convertSpecialChat(params.get("auditor")) + "%");
			}
			if(Strings.isNotBlank(params.get("review"))) {//按复核人查询
				buffer.append(" and summary.review like :review");
				paramMap.put("review", "%" + GovdocUtil.convertSpecialChat(params.get("review")) + "%");
			}
			if(Strings.isNotBlank(params.get("undertaker"))) {//按承办人查询
				buffer.append(" and summary.undertaker like :undertaker");
				paramMap.put("undertaker", "%" + GovdocUtil.convertSpecialChat(params.get("undertaker")) + "%");
			}
			if(Strings.isNotBlank(params.get("undertakenoffice"))) {//按承办机构查询
				buffer.append(" and summary.undertakenoffice like :undertakenoffice");
				paramMap.put("undertakenoffice", "%" + GovdocUtil.convertSpecialChat(params.get("undertakenoffice")) + "%");
			}
			if(Strings.isNotBlank(params.get("issuer"))) {//按签发人查询
				buffer.append(" and summary.issuer like :issuer");
				paramMap.put("issuer", "%" + GovdocUtil.convertSpecialChat(params.get("issuer")) + "%");
			}
			if(Strings.isNotBlank(params.get("printer"))) {//按打印人查询
				buffer.append(" and summary.printer like :printer");
				paramMap.put("printer", "%" + GovdocUtil.convertSpecialChat(params.get("printer")) + "%");
			}
			if(Strings.isNotBlank(params.get("printUnit"))) {//按印发单位查询
				buffer.append(" and summary.printUnit like :printUnit");
				paramMap.put("printUnit", "%" + GovdocUtil.convertSpecialChat(params.get("printUnit")) + "%");
			}
			if(Strings.isNotBlank(params.get("sendType"))) {//按行文类型查询
				buffer.append(" and summary.sendType like :sendType");
				paramMap.put("sendType", "%" + GovdocUtil.convertSpecialChat(params.get("sendType")) + "%");
			}
			if(Strings.isNotBlank(params.get("docType"))) {//按公文种类查询
				buffer.append(" and summary.docType = :docType");
				paramMap.put("docType",  GovdocUtil.convertSpecialChat(params.get("docType")));
			}
			if(Strings.isNotBlank(params.get("secretLevel"))) {//按密级查询
				buffer.append(" and summary.secretLevel like :secretLevel");
				paramMap.put("secretLevel", "%" + GovdocUtil.convertSpecialChat(params.get("secretLevel")) + "%");
			}
			if(Strings.isNotBlank(params.get("urgentLevel"))) {//按紧急程序查询
				buffer.append(" and summary.urgentLevel like :urgentLevel");
				paramMap.put("urgentLevel", "%" + GovdocUtil.convertSpecialChat(params.get("urgentLevel")) + "%");
			}
			if(Strings.isNotBlank(params.get("unitLevel"))) {//按公文级别查询
				buffer.append(" and summary.unitLevel like :unitLevel");
				paramMap.put("unitLevel", "%" + GovdocUtil.convertSpecialChat(params.get("unitLevel")) + "%");
			}
			if(Strings.isNotBlank(params.get("keepPeriod"))) {//按保密期限查询
				buffer.append(" and summary.keepPeriod = :keepPeriod");
				paramMap.put("keepPeriod", Integer.valueOf(params.get("keepPeriod")));
			}
			if(Strings.isNotBlank(params.get("flowState"))) {//按处理状态查询
				buffer.append(" and summary.state = :flowState");
				paramMap.put("flowState", Integer.valueOf(params.get("flowState")));
			}
			if(Strings.isNotBlank(params.get("createTime"))) {//按创建时间查询(待发列表)
				String time = params.get("createTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and summary.createTime >= :createTimeS");
					paramMap.put("createTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.createTime <= :createTimeE");
					paramMap.put("createTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("startTime"))) {//按发起时间查询
				String time = params.get("startTime").toString();
				String[] times = time.split("#");
                if (times.length > 0 && Strings.isNotBlank(times[0]) && (!",".equals(time))) {
					buffer.append(" and summary.createTime >= :startTimeS");
					paramMap.put("startTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.createTime <= :startTimeE");
					paramMap.put("startTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
//			zhou
			if (Strings.isNotBlank((String)params.get("summaryDeadLine"))) {
				String time = ((String)params.get("summaryDeadLine")).toString();
				String[] times = time.split("#");
				if (times.length > 0 && Strings.isNotBlank(times[0]) && !",".equals(time)) {
					buffer.append(" and summary.deadlineDatetime >= :startTimeS");
					paramMap.put("startTimeS", Datetimes.getTodayFirstTime(times[0]));
				}

				if (times.length > 1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.deadlineDatetime <= :startTimeE");
					paramMap.put("startTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}

			if(Strings.isNotBlank(params.get("packTime"))) {//按分送日期查询
				String time = params.get("packTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and summary.packTime >= :packTimeS");
					paramMap.put("packTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.packTime <= :packTimeE");
					paramMap.put("packTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("signingDate"))) {//按签发日期查询
				String time = params.get("signingDate").toString();
				String[] times = time.split("#");
				//客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]start
                if (times.length > 0 && Strings.isNotBlank(times[0]) && (!",".equals(time))) {
                    //客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]end
					buffer.append(" and summary.signingDate >= :signingDateS");
					paramMap.put("signingDateS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.signingDate <= :signingDateE");
					paramMap.put("signingDateE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("recieveDate"))) {//按签收日期查询
				String time = params.get("recieveDate").toString();
				String[] times = time.split("#");
				//客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]start
                if (times.length > 0 && Strings.isNotBlank(times[0]) && (!",".equals(time))) {
                    //客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]end
					buffer.append(" and summary.receiptDate >= :receiptDateS");
					paramMap.put("receiptDateS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.receiptDate <= :receiptDateE");
					paramMap.put("receiptDateE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("registerDate"))) {//按登记日期查询
				String time = params.get("registerDate").toString();
				String[] times = time.split("#");
				//客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]start
                if (times.length > 0 && Strings.isNotBlank(times[0]) && (!",".equals(time))) {
                    //客开 项目名称： 作者：fzc 修改日期：2018-4-3 [修改功能：]end
					buffer.append(" and summary.registrationDate >= :registrationDateS");
					paramMap.put("registrationDateS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and summary.registrationDate <= :registrationDateE");
					paramMap.put("registrationDateE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("completeTime"))) {//按处理时间查询(交换列表)
				String time = params.get("completeTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and affair.completeTime >= :completeTimeS");
					paramMap.put("completeTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and affair.completeTime <= :completeTimeE");
					paramMap.put("completeTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
		}
		return buffer.toString();
	}

	private String getHqlByStartMemberId(Map<String, Object> paramMap, Map<String, String> params) {
		StringBuilder buffer = new StringBuilder();
		List<Long> userIdList = GovdocUtil.getIdList(params.get("startUserId"));
		if (userIdList.size() == 1) {
			buffer.append(" and summary.startUserId = :startUserId");
			paramMap.put("startUserId", userIdList);
		} else if (userIdList.size() <= 300) {
			buffer.append(" and summary.startUserId in (:startUserId)");
			paramMap.put("startUserId", userIdList);
		} else {// 300个一组用or连接
			Map<String, List<Long>> senderIdMap = new HashMap<String, List<Long>>();
			List<Long> startUserIdList = new ArrayList<Long>();
			for (int i = 0; i < userIdList.size(); i++) {
				startUserIdList.add(userIdList.get(i));
				if (i != 0 && i % 300 == 0) {
					senderIdMap.put("startUserId" + i, startUserIdList);
					startUserIdList = new ArrayList<Long>();
				}
				if (i == userIdList.size() - 1) {
					senderIdMap.put("startUserId" + i, startUserIdList);
				}
			}
			buffer.append(" and (");
			int i = 0;
			for (String key : senderIdMap.keySet()) {
				if (i != 0) {
					buffer.append(" or ");
				}
				i++;
				List<Long> ids = senderIdMap.get(key);
				buffer.append(" summary.startUserId in (:" + key + ")");
				paramMap.put(key, ids);
			}
			buffer.append(" )");
		}
		return buffer.toString();
	}

	private String getExchangeHqlBySendUnitId(Map<String, Object> paramMap, Map<String, String> params) {
		StringBuilder buffer = new StringBuilder();
//		List<Long> sendUnitIdList = GovdocUtil.getIdList(params.get("exchangeSendUnitId"));
//		if (sendUnitIdList.size() == 1) {
//			buffer.append(" and detail.sendAccountId = :exchangeSendUnitId");
//			paramMap.put("exchangeSendUnitId", sendUnitIdList.get(0));
//		} else if (sendUnitIdList.size() <= 300) {
//			buffer.append(" and detail.sendAccountId in (:exchangeSendUnitId)");
//			paramMap.put("exchangeSendUnitId", sendUnitIdList);
//		} else {
//			// 300个一组用or连接
//			Map<String, List<Long>> sendAccountIdMap = new HashMap<String, List<Long>>();
//			List<Long> unitIdList = new ArrayList<Long>();
//			for (int i = 0; i < sendUnitIdList.size(); i++) {
//				unitIdList.add(sendUnitIdList.get(i));
//				if (i != 0 && i % 300 == 0) {
//					sendAccountIdMap.put("exchangeSendUnitId" + i, unitIdList);
//					unitIdList = new ArrayList<Long>();
//				}
//				if (i == unitIdList.size() - 1) {
//					sendAccountIdMap.put("exchangeSendUnitId" + i, unitIdList);
//				}
//			}
//			buffer.append(" and (");
//			int i = 0;
//			for (String key : sendAccountIdMap.keySet()) {
//				if (i != 0) {
//					buffer.append(" or ");
//				}
//				i++;
//				List<Long> ids = sendAccountIdMap.get(key);
//				buffer.append(" detail.sendAccountId in (:" + key + ")");
//				paramMap.put(key, ids);
//			}
//			buffer.append(" )");
//		}
		buffer.append(" and detail.sendUnit like :exchangeSendUnitName");
		paramMap.put("exchangeSendUnitName", "%" + params.get("exchangeSendUnitName") + "%");
		return buffer.toString();
	}

	/**
	 * 按affair表字段查询
	 * @param paramMap
	 * @param params
	 * @return
	 */
	private String getAffairHqlByCondition(Map<String, Object> paramMap, Map<String, String> params) {
		return getAffairHqlByCondition(paramMap, params, "affair");
	}
	private String getAffairHqlByCondition(Map<String, Object> paramMap, Map<String, String> params, String affairTable) {
		StringBuilder buffer = new StringBuilder();
		//客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-10 start
        List<Integer> subStateIds1 = new ArrayList<Integer>();
        subStateIds1.add(SubStateEnum.col_pending_specialBack.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackCenter.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderCancel.getKey());
        subStateIds1.add(SubStateEnum.col_pending_specialBackToSenderReGo.getKey());
        //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-10 end
		if(params.containsKey("condition")) {
			if(Strings.isNotBlank(params.get("importantLevel"))) {//按重要程序查询
				buffer.append(" and affair.importantLevel like :importantLevel");
				paramMap.put("importantLevel", "%" + GovdocUtil.convertSpecialChat(params.get("importantLevel")) + "%");
			}
			if(Strings.isNotBlank(params.get("draftState"))) {//按状态查询
				int subState = Integer.valueOf(params.get("draftState"));
				if(subState == SubStateEnum.col_waitSend_stepBack.key()) {
					List<Integer> subStateList = new ArrayList<Integer>();
					subStateList.add(subState);
					subStateList.add(SubStateEnum.col_pending_specialBacked.key());
					subStateList.add(SubStateEnum.col_waitSend_sendBack.key());
					subStateList.add(SubStateEnum.col_pending_specialBackToSenderCancel.key());
					buffer.append(" and affair.subState in (:draftState)");
					paramMap.put("draftState", subStateList);
				} else {
					buffer.append(" and affair.subState = :draftState");
					paramMap.put("draftState", subState);
				}
			}
			if(Strings.isNotBlank(params.get("dealState"))) {//按处理状态查询
                //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-10 start
                Integer subSate = Integer.valueOf(params.get("dealState"));
                if (String.valueOf(SubStateEnum.col_waitSend_stepBack.getKey()).equals(subSate+"")) {
                    //指定回退不允许暂存待办   普通回退经过暂存待办的  查询被回退的时候也应该过滤。
        	        subStateIds1.add(SubStateEnum.col_pending_ZCDB.getKey());
                    buffer.append(" and (affair.subState not in (:subStateIds) and affair.backFromId is not null )");
                    paramMap.put("subStateIds", subStateIds1);
                } else if (String.valueOf(SubStateEnum.col_pending_specialBack.getKey()).equals(subSate+"")) {
                    //指定回退
                    buffer.append(" and (affair.subState in (:subStateIds))");
                    paramMap.put("subStateIds", subStateIds1);
                } else if (String.valueOf(SubStateEnum.col_pending_read.getKey()).equals(subSate+"")) {
                    //已读
                    List<Integer> subStateIds2 = new ArrayList<Integer>();
                    subStateIds2.add(SubStateEnum.col_pending_read.getKey());//已读
                    subStateIds2.add(SubStateEnum.col_pending_ZCDB.getKey());//暂存待办
                    buffer.append(" and (affair.subState in (:subStateIds))");
                    paramMap.put("subStateIds", subStateIds2);
                } else {
                    buffer.append(" and affair.subState = :dealState");
                    paramMap.put("dealState", Integer.valueOf(params.get("dealState")));
                }
                //客开 项目名称： [修改功能：] 作者：fzc 修改日期：2018-5-10 end
            }
			if(Strings.isNotBlank(params.get("receiveTime"))) {//按接收时间查询
				String time = params.get("receiveTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and affair.receiveTime >= :receiveTimeS");
					paramMap.put("receiveTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and affair.receiveTime <= :receiveTimeE");
					paramMap.put("receiveTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("completeTime"))) {//按处理时间查询
				String time = params.get("completeTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and affair.completeTime >= :completeTimeS");
					paramMap.put("completeTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and affair.completeTime <= :completeTimeE");
					paramMap.put("completeTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
			if(Strings.isNotBlank(params.get("affairExpectedProcessTime"))) {//按处理期限查询
				String time = params.get("affairExpectedProcessTime").toString();
				String[] times = time.split("#");
				if(times.length>0 && Strings.isNotBlank(times[0])) {
					buffer.append(" and affair.expectedProcessTime is not null and affair.expectedProcessTime >= :affairExpectedProcessTimeS");
					paramMap.put("affairExpectedProcessTimeS", Datetimes.getTodayFirstTime(times[0]));
				}
				if(times.length>1 && Strings.isNotBlank(times[1])) {
					buffer.append(" and affair.expectedProcessTime is not null and affair.expectedProcessTime <= :affairExpectedProcessTimeE");
					paramMap.put("affairExpectedProcessTimeE", Datetimes.getTodayLastTime(times[1]));
				}
			}
		}
		return buffer.toString();
	}

    @SuppressWarnings("unchecked")
	public List<Long> getSummaryIdByPishi(Map<String, String> params) {
 		 if(!(Boolean)SysFlag.sys_isG6S.getFlag()){
			if (Strings.isNotBlank(params.get("pishiName")) || Strings.isNotBlank(params.get("pishiYear")) || Strings.isNotBlank(params.get("pishiNo"))) {
				Map<String, Object> queryParams = new HashMap<String, Object>();// 查询条件
				StringBuilder hql = new StringBuilder();
				hql.append(" select summaryId from EdocLeaderPishiNo pishi");
				hql.append(" where 1=1 ");
				if (Strings.isNotBlank(params.get("pishiName"))) {
					hql.append(" and pishi.pishiName = :psName ");
					queryParams.put("psName", SQLWildcardUtil.escape(params.get("pishiName")));
				}
				if (Strings.isNotBlank(params.get("pishiYear"))) {
					String pishiYear = params.get("pishiYear");
					hql.append(" and pishi.pishiYear like :psYear ");
					queryParams.put("psYear", "%"+SQLWildcardUtil.escape(pishiYear)+"%");
				}
				if (Strings.isNotBlank(params.get("pishiNo"))) {
					String pishiNo = params.get("pishiNo");
					hql.append(" and pishi.pishiNo = :psNo ");
					try {
						queryParams.put("psNo", Integer.valueOf(SQLWildcardUtil.escape(pishiNo)));
					} catch (Exception e) {
						queryParams.put("psNo", -1);
					}
				}
				hql.append(" group by pishi.summaryId");
				return (List<Long>)DBAgent.find(hql.toString(), queryParams);
			}
		}
		 return null;
	 }
	/************************ 以下为公文列表辅助Hql方法   end ***************************/

}
