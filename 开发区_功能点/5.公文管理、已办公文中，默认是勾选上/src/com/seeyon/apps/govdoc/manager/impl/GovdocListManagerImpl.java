package com.seeyon.apps.govdoc.manager.impl;

import com.seeyon.apps.agent.bo.AgentModel;
import com.seeyon.apps.agent.bo.MemberAgentBean;
import com.seeyon.apps.edoc.constants.EdocConstant.flowState;
import com.seeyon.apps.govdoc.constant.GovdocEnum;
import com.seeyon.apps.govdoc.constant.GovdocEnum.OldExchangeNodePolicyEnum;
import com.seeyon.apps.govdoc.constant.GovdocListEnum.GovdocListTypeEnum;
import com.seeyon.apps.govdoc.dao.GovdocListDao;
import com.seeyon.apps.govdoc.helper.GovdocHelper;
import com.seeyon.apps.govdoc.helper.GovdocListHelper;
import com.seeyon.apps.govdoc.manager.GovdocListConfigManager;
import com.seeyon.apps.govdoc.manager.GovdocListManager;
import com.seeyon.apps.govdoc.manager.GovdocSummaryManager;
import com.seeyon.apps.govdoc.util.GovdocUtil;
import com.seeyon.apps.govdoc.vo.GovdocListVO;
import com.seeyon.ctp.common.AppContext;
import com.seeyon.ctp.common.affair.util.WFComponentUtil;
import com.seeyon.ctp.common.constants.ApplicationCategoryEnum;
import com.seeyon.ctp.common.ctpenumnew.EnumNameEnum;
import com.seeyon.ctp.common.ctpenumnew.manager.EnumManager;
import com.seeyon.ctp.common.dump.vo.DumpDataVO;
import com.seeyon.ctp.common.exceptions.BusinessException;
import com.seeyon.ctp.common.i18n.ResourceUtil;
import com.seeyon.ctp.common.permission.bo.Permission;
import com.seeyon.ctp.common.permission.manager.PermissionManager;
import com.seeyon.ctp.common.po.ctpenumnew.CtpEnumBean;
import com.seeyon.ctp.common.workflowmanage.vo.WorkflowData;
import com.seeyon.ctp.organization.bo.V3xOrgAccount;
import com.seeyon.ctp.organization.bo.V3xOrgMember;
import com.seeyon.ctp.organization.manager.OrgManager;
import com.seeyon.ctp.util.DBAgent;
import com.seeyon.ctp.util.FlipInfo;
import com.seeyon.ctp.util.IdentifierUtil;
import com.seeyon.ctp.util.Strings;
import com.seeyon.v3x.edoc.domain.EdocSummary;

import java.util.*;

/**
 * 新公文列表管理类
 * @author 唐桂林
 *
 */
public class GovdocListManagerImpl implements GovdocListManager {

	private static final String startUserNameIsNotExists = "startUserNameIsNotExists";
	private static final String sendAccountNameIsNotExists = "sendAccountNameIsNotExists";

	private GovdocListDao govdocListDao;
	private GovdocListConfigManager govdocListConfigManager;
	private OrgManager orgManager;
	private EnumManager enumManagerNew;
	private PermissionManager permissionManager;
	private GovdocSummaryManager govdocSummaryManager;

	@Override
	public FlipInfo findPendingList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		condition = getGovdocAgentInfo(condition);
		String listType = condition.get("listType");
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if ("listExchangeSendPending".equals(listType)) {
			voList = govdocListDao.findSendPendingList(flipInfo, condition);
		}else if ("listExchangeSignPending".equals(listType)) {
			voList = govdocListDao.findSignPendingList(flipInfo, condition);
		}else{
			voList = govdocListDao.findList(flipInfo, condition);
		}
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		if (condition.containsKey("onlyCount")){//如果只统计count
			return flipInfo;
		}
		voList = setListVO(voList, condition);
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public int getPendingTotalCount(Map<String, String> condition)
			throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return 0;
		}
		condition = getGovdocAgentInfo(condition);
		String listType = condition.get("listType");
		if ("listExchangeSendPending".equals(listType)) {
			return govdocListDao.findSendPendingListCount(condition);
		}else if ("listExchangeSignPending".equals(listType)) {
			return govdocListDao.findSignPendingListCount(condition);
		}else{
			return govdocListDao.findListCount(condition);
		}
	}

	@Override
	public FlipInfo findDoneList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		if (sendAccountNameIsNotExists.equals(condition.get("exchangeSendUnitId"))) {
			return new FlipInfo();
		}
		condition = getGovdocAgentInfo(condition);
		String listType = condition.get("listType");
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if ("listExchangeSendDone".equals(listType)) {
			voList = govdocListDao.findSendDoneList(flipInfo, condition);
		}else if ("listExchangeSignDone".equals(listType)) {
			voList = govdocListDao.findSignDoneList(flipInfo, condition);
		}else if ("listExchangeFallback".equals(listType)) {
			voList = govdocListDao.findFallbackList(flipInfo, condition);
		}else{
			if(condition.containsKey("dumpData") && DumpDataVO.dataType.dumpData.getKey().equals(condition.get("dumpData"))){
				GovdocListDao govdocListDaoFK = (GovdocListDao) AppContext.getBean("govdocListDaoFK");
				if(govdocListDaoFK != null){
					voList = govdocListDaoFK.findList(flipInfo, condition);
				}
			}else{
				voList = govdocListDao.findList(flipInfo, condition);
			}
			voList = setListVO(voList, condition);
			flipInfo.setData(voList);
			return flipInfo;
		}
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo findSentList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		condition = getGovdocAgentInfo(condition);
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(condition.containsKey("dumpData") && DumpDataVO.dataType.dumpData.getKey().equals(condition.get("dumpData"))){
			GovdocListDao govdocListDaoFK = (GovdocListDao) AppContext.getBean("govdocListDaoFK");
			if(govdocListDaoFK != null){
				voList = govdocListDaoFK.findList(flipInfo, condition);
			}
		}else{
			voList = govdocListDao.findList(flipInfo, condition);
		}
		voList = setListVO(voList, condition);
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo findWaitSendList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		condition = getGovdocAgentInfo(condition);
		List<GovdocListVO> voList = govdocListDao.findList(flipInfo, condition);
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo find4QuoteList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = govdocListDao.find4QuoteList(flipInfo, condition);
		voList = setListVO(voList, condition);
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo findQueryResultList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = new ArrayList<GovdocListVO>();
		if(condition.containsKey("dataType") && DumpDataVO.dataType.dumpData.getKey().equals(String.valueOf(condition.get("dataType"))) && AppContext.hasPlugin("fk")){
			GovdocListDao govdocListDaoFK = (GovdocListDao) AppContext.getBean("govdocListDaoFK");
			if(govdocListDaoFK != null){
				voList = govdocListDaoFK.findQueryResultList(flipInfo, condition);
			}
		}else{
			voList = govdocListDao.findQueryResultList(flipInfo, condition);
		}
        voList = setListVO(voList, condition);
        if(flipInfo.getSize() == -1){
        	flipInfo.setSize(voList.size());
        }
        DBAgent.memoryPaging(voList, flipInfo);
		return flipInfo;
	}

	@Override
	public FlipInfo findSendRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
	    if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = govdocListDao.findRegisterList(flipInfo, condition);
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo findSignRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
		if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = govdocListDao.findRegisterList(flipInfo, condition);
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}

	@Override
	public FlipInfo findRecRegisterList(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
        if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = govdocListDao.findRegisterList(flipInfo, condition);
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}

	/**
	 * 设置查询条件
	 * @param type
	 * @param condition
	 * @return
	 */
	private Map<String, String> setGovdocListCondition(Map<String, String> condition) throws BusinessException {
		for(String key : condition.keySet()){
			if (Strings.isNotBlank(key) && key.indexOf("from_") >= 0) {
				String curKey = key.replace("from_", "");
				if (Strings.isNotBlank(key) && Strings.isNotBlank(condition.get(key)) && Strings.isBlank(condition.get(curKey))) {
					condition.put(curKey, condition.get(key));
				}
			}
		}
		String listType = condition.get("listType");
		if(Strings.isBlank(condition.get("memberId"))) {
			condition.put("memberId", String.valueOf(AppContext.currentUserId()));
		}
		if(Strings.isBlank(condition.get("orgAccountId"))) {
			condition.put("orgAccountId", String.valueOf(AppContext.currentAccountId()));
		}
		if(Strings.isBlank(condition.get("departmentId"))) {
			condition.put("departmentId", String.valueOf(AppContext.getCurrentUser().getDepartmentId()));
		}
		if(Strings.isBlank(condition.get("needTotal"))) {
			condition.put("needTotal", "true");
		}
		//若listType为已办、在办、已办结，则默认为列表只显示一条
		if(condition.get("listType").startsWith("listDone") || condition.get("listType").startsWith("listFinished")) {
			if(Strings.isBlank(condition.get("deduplication"))) {
//				zhou:修改默认就是显示一条
				condition.put("deduplication", "true");
			}
		}

		if(listType.endsWith("Register")) {//公文登记簿，若govdocType为空，则赋值
			condition.put("govdocType", GovdocListTypeEnum.getGovdocTypeName(listType));
		}

		// 在这里， 用模糊查询， 把发起人的id都查询出来，  然后最后拼接hql，匹配
		if(Strings.isNotBlank(condition.get("startUserName"))) {//按发起人查询
			StringBuilder startUserIds = new StringBuilder("");
			List<V3xOrgMember> senderList = orgManager.getMemberByIndistinctName(condition.get("startUserName"));
			int i=0;//OA-191840
			for (V3xOrgMember sender : senderList) {
				if (i < 999) {
					if (startUserIds.length() > 0) {
						startUserIds.append(",");
					}
					startUserIds.append(sender.getId());
				}
				i++;
			}
			condition.remove("startUserName");
			if (startUserIds.length() ==  0) {
				condition.put("startUserId", startUserNameIsNotExists);
			} else {
				condition.put("startUserId", startUserIds.toString());
			}
		}

		if(Strings.isNotBlank(condition.get("configId"))) {//列表分类自定义配置
			if(govdocListConfigManager == null) {
				govdocListConfigManager = (GovdocListConfigManager) AppContext.getBean("govdocListConfigManager");
			}
			String nodePolicy = govdocListConfigManager.getNodePolicyByConfigId(Long.parseLong(condition.get("configId")));
			if(Strings.isNotBlank(nodePolicy)) {
				condition.put("nodePolicy", nodePolicy);
			}
		} else {
			String nodePolicy = GovdocListTypeEnum.getNodePolicyName(listType);
			String notInNodePolicy = GovdocListTypeEnum.getNotInNodePolicyName(listType);
			if(Strings.isNotBlank(nodePolicy)) {
				condition.put("nodePolicy", nodePolicy);
				//待分送/待签收/已签收
				if (listType.equals(GovdocListTypeEnum.listExchangeSendPending.getKey())  || listType.equals(GovdocListTypeEnum.listExchangeSignPending.getKey()) || listType.equals(GovdocListTypeEnum.listExchangeSignDone.getKey())) {
					Long orgAccountId = Long.valueOf(condition.get("orgAccountId"));
					EnumNameEnum categoryEnum = null;
					if (listType.equals(GovdocListTypeEnum.listExchangeSendPending.getKey())) {
						categoryEnum = EnumNameEnum.edoc_new_send_permission_policy;
					} else {
						categoryEnum = EnumNameEnum.edoc_new_change_permission_policy;
					}
					String policys = "";
					List<Permission> permissions = permissionManager.getPermissionsByCategory(categoryEnum.name(),orgAccountId);
					if(Strings.isNotEmpty(permissions)) {
						String[] nodePolicys = nodePolicy.split(",");
						for(String policy : nodePolicys) {
							if(OldExchangeNodePolicyEnum.oldfasong.name().equals(policy) || OldExchangeNodePolicyEnum.oldqianshou.name().equals(policy)) {
								if (Strings.isNotBlank(policys)) {
									policys += ",";
								}
								policys += policy;
								continue;
							}
							for(Permission bean : permissions) {
								if(bean.getBasicOperation().contains(policy)){
									if (Strings.isNotBlank(policys)) {
										policys += ",";
									}
									policys += bean.getName();
								}
							}
						}
					}
					condition.put("nodePolicy", policys);
				}
			}

			if(Strings.isNotBlank(notInNodePolicy)) {
				condition.put("notInNodePolicy", notInNodePolicy);
			}
		}

		return condition;
	}

	/**
	 * 代理人处理
	 * @param condition
	 * @param userId
	 * @return
	 */
	@SuppressWarnings("unused")
	private Map<String, String> getGovdocAgentInfo(Map<String, String> condition) {
		Long memberId = Long.parseLong(condition.get("memberId"));
		//获取代理相关信息
  		List<AgentModel> _agentModelList = MemberAgentBean.getInstance().getAgentModelList(memberId);
      	List<AgentModel> _agentModelToList = MemberAgentBean.getInstance().getAgentModelToList(memberId);
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
      			if((agentModel.isHasEdocFree() || agentModel.isHasEdocTemplate())  && agentModel.getStartDate().before(now) && agentModel.getEndDate().after(now)){
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
  		/*condition.put("edocAgent", edocAgent);*/
  		condition.put("agentToFlag", agentToFlag+"");
  		condition.put("agentFlag", agentFlag+"");
  		return condition;
	}

	/**
     *
     * @param request
     * @param response
     * @param modelAndView
     * @param queryList
     * @param condition
     * @return
     */
    @SuppressWarnings("unchecked")
	private List<GovdocListVO> setListVO(List<GovdocListVO> voList, Map<String, String> condition) throws BusinessException {
    	String listType = condition.get("listType");
    	Long memberId = Long.parseLong(condition.get("memberId"));
    	//boolean agentFlag = GovdocParamUtil.getBoolean(condition, "agentFlag") || GovdocParamUtil.getBoolean(condition, "agentToFlag");
    	CtpEnumBean docType = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_doc_type.name());//公文种类
    	CtpEnumBean sendType = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_send_type.name());//行文类型
		CtpEnumBean secretLevel = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_secret_level.name());//密级
		CtpEnumBean urgentLevel = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_urgent_level.name());//紧急
		CtpEnumBean unitLevel = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_unit_level.name());//公文类别
		CtpEnumBean keepPeriod = enumManagerNew.getEnumByProCode(EnumNameEnum.edoc_keep_period.name());//保存期限
		//性能提升，预归档路径缓存
		Map<String,String> docPathMap = new HashMap<String, String>();
		//性能提升，领导批示
		Map<Long,String> leaderPishiMap = new HashMap<Long, String>();
		List<Long> summaryIds = new ArrayList<Long>();
		for(GovdocListVO listVo : voList) {
			summaryIds.add(listVo.getSummaryId());
		}
		for(GovdocListVO listVo : voList) {
			//附件区分
    		listVo.setHasAtt(IdentifierUtil.lookupInner(listVo.getIdentifier(), EdocSummary.INENTIFIER_INDEX.HAS_ATTACHMENTS.ordinal(), '1'));
    		//发起人/拟稿人
    		V3xOrgMember startUser = orgManager.getMemberById(listVo.getStartUserId());
    		listVo.setStartUserName(startUser == null ? "" : GovdocUtil.getMemberName(startUser.getId()));
    		//当前待办人
    		if(listVo.getSummaryState() != null && listVo.getSummaryState() == flowState.terminate.ordinal()){
    			listVo.setCurrentNodesInfo( ResourceUtil.getString("collaboration.list.finished.label"));
    		}else{
    			listVo.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo(listVo.getCompleteTime(), listVo.getCurrentNodesInfo(), Collections.EMPTY_MAP));
    		}
    		//流程期限
    		listVo.setSummaryDeadLineName(GovdocHelper.getDeadLineName(listVo.getSummaryDeadlineDatetime()));
    		//节点期限
    		if(listVo.getAffairExpectedProcessTime() != null) {
            	listVo.setAffairDeadLineName(WFComponentUtil.getDeadLineName(listVo.getAffairExpectedProcessTime()));
            } else {
            	listVo.setAffairDeadLineName(WFComponentUtil.getDeadLineName(listVo.getAffairDeadlineDate()));
            }
            //设置交换状态(未结束/已结束/已分送)
    		if(listVo.getSummaryTransferStatus() != null && listVo.getSummaryTransferStatus().intValue()>=2) {
    			listVo.setSummaryTransferStatusName(ResourceUtil.getString(GovdocEnum.TransferStatus.getByKey(listVo.getSummaryTransferStatus()).getLabel()));
    		} else if (listVo.getSummaryState() != null){
    			if(listVo.getSummaryState() != null) {
	    			if(listVo.getSummaryState().intValue() == 0) {
	    				listVo.setSummaryTransferStatusName(ResourceUtil.getString("govdoc.Notfinished"));
	    			} else if(listVo.getSummaryState().intValue() == 1 || listVo.getSummaryState().intValue() == 3) {
	                    if (listVo.getExchangeSendAffairId() == null || listVo.getExchangeSendAffairId() == -1) {
	                        listVo.setSummaryTransferStatusName(ResourceUtil.getString("govdoc.Hasended"));
	                    }else{
	                        listVo.setSummaryTransferStatusName(ResourceUtil.getString("govdoc.sent"));
	                    }
	    			}
    			}
    		}
    		//设置督办时间
            listVo.setSurplusTime(GovdocListHelper.calculateSurplusTime(listVo.getAffairReceiveTime(), listVo.getAffairExpectedProcessTime()));
            //公文枚举值设置
            GovdocListHelper.fillListEnumLabel(listVo, docType, sendType, keepPeriod, secretLevel, urgentLevel, unitLevel);
           	//是否归档和归档路径
           	if("listQuery".equals(listType)) {
           		GovdocListHelper.fillListArchiveData(listVo,docPathMap);
           	}
           	//列表展示动态标题
	       	if(Strings.isNotBlank(listVo.getAffairSubject())) {
	       		listVo.setSubject(listVo.getAffairSubject());
	       	}
	       	//自动发起标题
	       	if(listVo.getAutoRun()){
	       		listVo.setSubject(ResourceUtil.getString("collaboration.newflow.fire.subject",listVo.getSubject()));
	       	}
	       	//设置代理文字
           	if(listVo.getAffairMemberId()!=null && memberId!=null
           			&& !"niwen".equals(listVo.getAffairNodePolicy())) {
				GovdocListHelper.fillListAgent(listVo, true, memberId, listType);
			}

           	if(GovdocUtil.isExchangeSendPendingList(listType)) {
				// 性能问题，上一节点处理人使用preApprover
				// GovdocWorkflowHelper.fillLastProcessLog(listVo);
           		if(listVo.getAffairPreApprover() != null){
           			listVo.setPreUserName(orgManager.getMemberById(listVo.getAffairPreApprover()).getName());
           		}
           	} else if(GovdocUtil.isExchangeSignList(listType)) {
           		if(listVo.getOrgAccountId() != null) {
           			V3xOrgAccount account = orgManager.getAccountById(listVo.getOrgAccountId());
           			if(account != null) {
           				listVo.setExchangeSendUnitName(account.getName());
           			}
           		}
           	}
           	listVo.setLeaderCommondNo(GovdocListHelper.getAllLeaderPishi(leaderPishiMap, summaryIds, listVo.getSummaryId()));
           	if (listVo.getAffairTrack() == null) {
           		listVo.setAffairTrack(0);
			}
    	}
    	return voList;
    }

	@Override
	public List<WorkflowData> getAdminWfDataList(FlipInfo flipInfo, Map<String,Object> conditionParam, long accountId, boolean isPage, FlipInfo fi)  throws BusinessException {
//		return govdocListDao.getAdminWfDataList(flipInfo, conditionParam, accountId, isPage,fi);
		String currentNodesInfo = (String)conditionParam.get("currentNodesInfo");
		List<WorkflowData> result = null;
		if (Strings.isNotBlank(currentNodesInfo)) {
			List<Long> objectIds = (List)conditionParam.get("objectIds");
			List<EdocSummary> edocSummary = this.govdocSummaryManager.findEdocSummarysByIds(objectIds);
			String flow = (String)conditionParam.get("flowstate");
			int flowstate = 0;
			if (Strings.isNotBlank(flow)) {
				flowstate = Integer.parseInt(flow);
			}

			result = this.buildResult(edocSummary, flowstate);
		} else {
			result = this.govdocListDao.getAdminWfDataList(flipInfo, conditionParam, accountId, isPage, fi);
		}

		return result;
	}
	public List<WorkflowData> buildResult(List<EdocSummary> result, int flowstate) throws BusinessException {
		List<WorkflowData> list = new ArrayList();

		WorkflowData flow;
		for(Iterator var4 = result.iterator(); var4.hasNext(); list.add(flow)) {
			EdocSummary summary = (EdocSummary)var4.next();
			flow = new WorkflowData();
			flow.setEdocType(-1);
			flow.setSummaryId(String.valueOf(summary.getId()));
			int app = ApplicationCategoryEnum.edoc.getKey();
			flow.setAppEnum(app);
			flow.setSubject(summary.getSubject());
			Long startUserId = summary.getStartUserId();
			if (startUserId != null) {
				V3xOrgMember member = null;

				try {
					member = this.orgManager.getMemberById(startUserId);
				} catch (BusinessException var12) {
				}

				if (member != null) {
					flow.setInitiator(member.getName());
				}
			}

			flow.setSendTime(summary.getCreateTime());
			flow.setProcessId(summary.getProcessId());
			flow.setCaseId(summary.getCaseId());
			flow.setDeadLine(summary.getDeadline());
			flow.setAdvanceRemind(summary.getAdvanceRemind());
			flow.setAccountId(summary.getOrgAccountId());
			Long templeteId = summary.getTempleteId();
			if (templeteId != null) {
				flow.setIsFromTemplete(true);
				flow.setTempleteId(templeteId);
			}

			GovdocUtil.setWorkflowDataByEdocSummary(summary, flow);
			Date deadlineDatetime = summary.getDeadlineDatetime();
			flow.setDeadlineDatetimeName(WFComponentUtil.getDeadLineName(deadlineDatetime));
			String currentNodesInfo = summary.getCurrentNodesInfo();
			if (flowstate != flowState.terminate.ordinal() && flowstate != flowState.finish.ordinal()) {
				if (Strings.isNotBlank(currentNodesInfo) && currentNodesInfo.indexOf(";;") != -1) {
					currentNodesInfo = currentNodesInfo.replaceAll(";;", ";");
				}
			} else {
				currentNodesInfo = "";
			}

			summary.setCurrentNodesInfo(currentNodesInfo);
			flow.setCurrentNodesInfo(GovdocHelper.parseCurrentNodesInfo(summary));
			flow.setSecretLevel(summary.getSecretLevel());
			flow.setFormAppId(summary.getFormAppid());
			flow.setFormRecordId(summary.getFormRecordid());
			if (flowstate == 1 || flowstate == 3) {
				flow.setEndFlag(0);
			}
		}

		return list;
	}

	@Override
	public Integer getRegisterCount(Map<String, String> condition) throws BusinessException {
		return govdocListDao.getRegisterCount(condition);
	}

	@Override
	public FlipInfo getList4Xiaoz(FlipInfo flipInfo, Map<String, String> condition) throws BusinessException {
		condition = setGovdocListCondition(condition);
        if (startUserNameIsNotExists.equals(condition.get("startUserId"))) {
			return new FlipInfo();
		}
		List<GovdocListVO> voList = govdocListDao.findList(flipInfo, condition);
		voList = setListVO(voList, condition);
		if(flipInfo == null) {
			flipInfo = new FlipInfo();
		}
		flipInfo.setData(voList);
		return flipInfo;
	}
    /*************************** 99999 Spring注入，请将业务写在上面 start ******************************/
	public void setGovdocListDao(GovdocListDao govdocListDao) {
		this.govdocListDao = govdocListDao;
	}
	public void setOrgManager(OrgManager orgManager) {
		this.orgManager = orgManager;
	}
	public void setEnumManagerNew(EnumManager enumManagerNew) {
		this.enumManagerNew = enumManagerNew;
	}
	public void setGovdocListConfigManager(GovdocListConfigManager govdocListConfigManager) {
		this.govdocListConfigManager = govdocListConfigManager;
	}
	public void setPermissionManager(PermissionManager permissionManager) {
		this.permissionManager = permissionManager;
	}
	/*************************** 99999 Spring注入，请将业务写在上面   end ******************************/

	public void setGovdocSummaryManager(GovdocSummaryManager govdocSummaryManager) {
		this.govdocSummaryManager = govdocSummaryManager;
	}
}
